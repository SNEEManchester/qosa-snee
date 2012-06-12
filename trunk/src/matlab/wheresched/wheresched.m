function f = wheresched(x, p)

global rt;
global operatorinstances;
global parentOpInst;
global outputSize;
global opInstanceIndex;
global min_f;
global sites;
global opInstOperator;
global card;
global siteTuplesToSend;
global operators;
global tuplesPerMessage;
global txEnergy;
global siteChildren;
global rxEnergy;
global energyCost;
global energyAvailable;
global opt_goal;

disp('*** Starting Objective Function Calculation ***');

%Not currently being used: getTotalDataSizeTransmitted();
if (strcmp(opt_goal,'minimize delivery time') || strcmp(opt_goal,'minimize acquisition interval'))
	getDeliveryTime();
elseif strcmp(opt_goal, 'minimize total energy')==true
	getTotalEnergy();
elseif strcmp(opt_goal, 'maximize lifetime')==true
	getNetworkLifetime();
else
	error(sprintf('Optimization goal "%s" not supported by QoS-aware where scheduling'));
end


disp(sprintf('OBJ FUNCTION=%d',f))
if (f < min_f) 
	min_f=f;
end


return;

function [] = getDeliveryTime()
	totalPacks = 0;
	tuplesToSend = getSiteTuplesToSend();

	for i=1:1:length(sites)
		siteId = sites{i};

		tupleCount = countSiteTuples(siteId, tuplesToSend);
		weighting  = 1; %We are just interested in number of packets...
		totalPacks = countSitePacketEnergy(totalPacks, tupleCount, weighting);
		clear tupleCount
	end
	
	f = totalPacks;
end


function [] = getTotalEnergy()
	tuplesToSend = getSiteTuplesToSend();
	[totalNetworkEnergyConsumption, siteEnergyConsumption] = computeSiteEnergyConsumption(tuplesToSend);
	f = totalNetworkEnergyConsumption;
end


function [] = getNetworkLifetime()
	tuplesToSend = getSiteTuplesToSend();
	[totalNetworkEnergyConsumption, siteEnergyConsumption] = computeSiteEnergyConsumption(tuplesToSend);

	shortestLifetime = Inf;
	shortestLifetimeSite = NaN;

	lifetimeList = [];

	%Get the lifetime for each site
	numSites = length(sites);
	for i=1:1:numSites
		siteId = sites{i};
		siteEnergyCons = get(siteEnergyConsumption, siteId);
		siteEnergyStock = get(energyAvailable, siteId);
		lifetime = siteEnergyStock / siteEnergyCons;
		
		lifetimeList = [lifetimeList lifetime];
		
		if lifetime < shortestLifetime
			shortestLifetime = lifetime;
			shortestLifetimeSite = siteId;
		end
	end

%	disp(sprintf('Site with the shortest lifetime is %d', shortestLifetimeSite));
%	f = 1/shortestLifetime;

	lifetimeList = sort(lifetimeList, 'ascend');
%	weightedLifetime = 0.5*lifetimeList(1) + 0.35*lifetimeList(2) + 0.15*lifetimeList(3)
	n = (numSites*(numSites+1))/2;
	weightedLifetime = 0;
	for i=1:1:numSites
		weightedLifetime = weightedLifetime + lifetimeList(i)*((numSites-i+1)/n);
	end
	
	f = 1/weightedLifetime;	
	
	
end

function [totalNetworkEnergyConsumption, siteEnergyConsumption] = computeSiteEnergyConsumption(tuplesToSend)

	siteEnergyConsumption = hashtable;
	totalNetworkEnergyConsumption = 0;
	
	for i=1:1:length(sites)
		siteId = sites{i};

		sumEnergyConsumption = 0;
		%sum energy of packets sent from this site
		tupleCount = countSiteTuples(siteId, tuplesToSend);
		txEdgeCost = get(txEnergy, siteId);
		weighting  = 1; %This would be site TX cost for energy
		sumEnergyConsumption = countSitePacketEnergy(sumEnergyConsumption, tupleCount, weighting);
		clear tupleCount

		%sum energy of packets received at this site
		children = get(siteChildren, siteId);
		for j=1:1:length(children)
			childId = children{j};

			tupleCount = countSiteTuples(childId, tuplesToSend);
			sumEnergyConsumption = countSitePacketEnergy(sumEnergyConsumption, tupleCount, rxEnergy);
			clear tupleCount;
		end

		%sum energy of operators executing at this site
		for j=1:1:length(p)
			if p{j}==siteId %i.e., operator is placed at this site
				opInstId = operatorinstances{j};
				opInstEnergyCost = get(energyCost, opInstId);
				sumEnergyConsumption = sumEnergyConsumption + opInstEnergyCost;
			end
		end
		
		totalNetworkEnergyConsumption = totalNetworkEnergyConsumption + sumEnergyConsumption;
		siteEnergyConsumption = put(siteEnergyConsumption, siteId, sumEnergyConsumption);
	end
end


%Creates hashtable which maps siteID to an array of tuples to send
function tuplesToSend = getSiteTuplesToSend()
	tuplesToSend = hashtable;
	for i = 1:1:length(sites)
		tuplesToSend = put(tuplesToSend, sites{i}, []);
	end
	
	for i=1:1:length(operatorinstances)
	    sourceOpInst = operatorinstances{i};

	    if has_key(parentOpInst, sourceOpInst)
		destOpInst = get(parentOpInst, sourceOpInst);
		sourceOpInstIndex = get(opInstanceIndex, sourceOpInst);
		destOpInstIndex = get(opInstanceIndex, destOpInst);

		sourceSite = p{sourceOpInstIndex};
		destSite = p{destOpInstIndex};
		path = find_path(rt,sourceSite+1,destSite+1); %+1 adjustment

		if length(sites)==1
			disp('Warning: There appears to be only one node in the graph.');
			path = [sourceSite];
		elseif isempty(path)==1
			error('ERROR: Site %d and site %d do not appear to be connected, or there is only one node in the graph.',sourceSite,destSite);
		end

		for i=1:1:(length(path) - 1)
			siteId = path(i)-1; %+1 adjustment
			if has_key(tuplesToSend, siteId)
				siteTuplesToSend = get(tuplesToSend, siteId);
				siteTuplesToSend{length(siteTuplesToSend)+1} = sourceOpInst;
				tuplesToSend = put(tuplesToSend, siteId, siteTuplesToSend);
			else
				error('ERROR: You should never see this.');
			end
			
		end
	    end
	end
end


%Creates hashtable for a given site which maps type of tuple to number of tuples to send of that type
function tupleCount = countSiteTuples(siteId, tuplesToSend)
	tupleCount = hashtable;
	siteTuplesToSend = get(tuplesToSend, siteId);		
	for j=1:1:length(siteTuplesToSend)
		opInst = siteTuplesToSend{j};
		type = get(opInstOperator, opInst);
		opInstCard = get(card, opInst);
		if has_key(tupleCount, type)
			count = get(tupleCount, type);
			count = count + opInstCard;
			tupleCount = put(tupleCount, type, count);
		else
			tupleCount = put(tupleCount, type, opInstCard);
		end			
	end
end

%Given the tupleCount hashtable for a particular site, computes the number of packets required, multiplies it by a weighting (if applicable)
%and adds it to energySum, the grand total.    If weighting==1, we are just counting packets (i.e, as is done for delivery time....)
function [energySum] = countSitePacketEnergy(energySum, tupleCount, weighting)
	for j=1:1:length(operators)
		opId = operators{j};

		if has_key(tupleCount,opId)
			tupsPerMsg = get(tuplesPerMessage, opId);
			tupCount = get(tupleCount, opId);
			%numPacks = ceil(tupCount / tupsPerMsg); %ceil does the padding... does not scale with beta>1
			numPacks = tupCount / tupsPerMsg;
			energySum = energySum + (numPacks * weighting);
		end
	end
end


function [] = getTotalDataSizeTransmitted()
	f = 0;
	for i=1:1:length(operatorinstances)
	    sourceOpInst = operatorinstances{i};

	    if has_key(parentOpInst, sourceOpInst)
		destOpInst = get(parentOpInst, sourceOpInst);

		size = get(outputSize,sourceOpInst);

		sourceOpInstIndex = get(opInstanceIndex, sourceOpInst);
		destOpInstIndex = get(opInstanceIndex, destOpInst);

		sourceSite = p{sourceOpInstIndex};
		destSite = p{destOpInstIndex};
		path = find_path(rt,sourceSite+1,destSite+1); %+1 adjustment

		if length(sites)==1
			disp('Warning: There appears to be only one node in the graph.');
			path = [sourceSite];
		elseif isempty(path)==1
			error('ERROR: Site %d and site %d do not appear to be connected, or there is only one node in the graph.',sourceSite,destSite);
		end
		f = f + (length(path) - 1) * size;
	    end
	end    
end

end