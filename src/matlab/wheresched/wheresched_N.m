%*********************************************************************
% experiment_N:  User-supplied function defining set of neighbors for a
%             a given vector of categorical variables p.
% --------------------------------------------------------------------
%   Variables:
%     iterate = iterate for whom discrete neighbor point will be found.
%     plist   = cell array of possible p values: p{i}{j}, i = variable, j = list
%     Problem = structure holding Omega(p)
%     N       = vector of iterates who are neighbors of p.
%*********************************************************************
% * Choose a non-location sensitive operator instance at random.
% * Choose a random site between its deepest confluence site and the sink (henceforth deemed the "new" site)
% * Shift child operators leafwards, or parent operators towards the root, as necessary to avoid circular tuple flows.  For example, if moving an operator leafwards, move all its descendants which are on the path from the current site to the new site to the new site.  Conversely, if moving an operator rootwards, move all its ascendants which are on the path from the current site to the new site to the new site.
function N = wheresched_N(Problem,iterate,plist,delta)

global operatorinstances;
global locationConstraints;
global rt;
global opInstDeepestConfSite;
global opInstChildren;
global opInstanceIndex;
global parentOpInst;
global numNeighbours
global sites;
global searchTreeFile;
global pidDAFidMap;

currentPID = generatePointID(iterate.p);
currentCost = wheresched(iterate.x,iterate.p);
dafId = pidDAFidMap{currentPID}

[h,ErrMsg] = fopen(searchTreeFile,'at');
error(ErrMsg);
tmpStr = sprintf('"%g" [shape=doublecircle fontsize=5 label = "%g\\ncost=%g\\ndafId=%s"]',currentPID,currentPID,currentCost,dafId);
fwrite(h,tmpStr,'char');
fclose(h);

if numNeighbours==0
	%Deal with x, the continuous variable.  Needed for solver to work.
	N(1).x = iterate.x;

	%Assign to current point
	N(1).p = iterate.p;
end


disp(sprintf('\n*** Starting Neighbour Function for point id=%d, DAF id=%s***',currentPID, pidDAFidMap{currentPID}));

%produce a number of neighbours, changing values for non-location sensitive
%operators; there's a possiblity of the Neighbours being the same, this is 
%probably okay.
candNum = 1;
for i=1:1:numNeighbours

	%Deal with x, the continuous variable.  Needed for solver to work.
	N(candNum).x = iterate.x;

	%Assign to current point
	N(candNum).p = iterate.p;
	
	disp(sprintf('** Attempt %d to generate candidate (candidate number=%d)',i,candNum))

	% * Choose a non-location sensitive operator instance at random.
	numOpInstances = length(operatorinstances);
	opInstIndex = ceil(rand*numOpInstances);
	opInstId = operatorinstances{opInstIndex};
		
	if isLocSen(opInstId)
		disp(sprintf('FAILED: operator inst %s is location sensitive', opInstId));
	else
		currentSite = N(candNum).p{opInstIndex};

		% * Choose a random site between its deepest confluence site and 
        	% the sink (henceforth deemed the "new" site)
		% Gets the deepest confluence site
		deepestPossSite = get(opInstDeepestConfSite, opInstId);
		
		% Get a list of all the confluence sites for an operator instance, i.e., all the sites where the opInst could be placed
		if length(sites)>1
			possibleSites = find_path(rt,deepestPossSite+1,0+1); %+1 adjustment		
		else
			possibleSites = sites{1};
		end
		
		%Now choose a site at random from the list of possible sites
		numSites = length(possibleSites);
		siteIndex = ceil(rand*numSites);
		newSite = possibleSites(siteIndex)-1; %+1 adjustment

		if currentSite~=newSite

			N = daf_moveOpInst(N, candNum, opInstIndex, opInstId, currentSite, newSite);

			newPID = generatePointID(N(candNum).p);
			dafId = pidDAFidMap{newPID};
			newCost = wheresched(N(candNum).x,N(candNum).p);
			disp(sprintf('new point id=%d, DAF id=%s, objective function value=%d',newPID,dafId,newCost));

			%add search tree
			[h,ErrMsg] = fopen(searchTreeFile,'at');
			error(ErrMsg);
			tmpStr = sprintf('"%g" -> "%g";\n',currentPID,newPID);
			fwrite(h,tmpStr,'char');
			tmpStr = sprintf('"%g" [fontsize=5 label = "%g\\ncost=%g\\ndafId=%s"];\n',newPID,newPID,newCost,dafId);
			fwrite(h,tmpStr,'char');    
			fclose(h);

			%check that each pID points to unique assignment
			[h,ErrMsg] = fopen('search-points.csv','at');
			error(ErrMsg);
			tmpStr=sprintf('%d,%d,%d,',candNum,currentPID,newPID);
			for k=1:1:length(N(candNum).p)
				tmpStr = strcat(tmpStr,sprintf('%d,',N(candNum).p{k}));
			end
			fprintf(h,'%s\n',tmpStr);
			fclose(h);

			displayAssignment(N(candNum).p);
			candNum = candNum + 1;
		else	
			disp(sprintf('FAILED: operator inst %s stayed at same location', opInstId));	
		end
		
	end
end

return;

end
