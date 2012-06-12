function iterate = wheresched_x0;

global assignment;
global outputSize;
global opInstChildren;
global opInstDeepestConfSite;
global operatorinstances;
global locationConstraints;
global initVal;
global heuristic_init_point;

disp('*** Starting Initial Points Function***')

%This one is guaranteed to be feasible
iterate(1).x = 1; %keep this constant.
iterate(1).p=assignment;

newPID = generatePointID(iterate(1).p);
newCost = wheresched(iterate(1).x,iterate(1).p);
initVal = newCost;
disp(sprintf('initial point 1 id=%d, objective function value=%d',newPID,newCost));

%Generate second point, using heuristics, which may not be feasible wrt memory
if strcmp(heuristic_init_point,'true')
	numOpInstances = length(operatorinstances);
	iterate(2).x=1;
	iterate(2).p=assignment;

	for i=1:1:numOpInstances
		opInstId = operatorinstances{i};
		currentSite = iterate(2).p{i};
	
		if ~has_key(locationConstraints, opInstId)
		
			if isDataDecreasing(opInstId)==1
		
				%TODO: Is this the new site index? If so, modify comment in planning.m
				newSite = get(opInstDeepestConfSite, opInstId);
		
				if currentSite ~= newSite
					iterate = daf_moveOpInst(iterate, 2, i, opInstId, currentSite, newSite);				
				end		
			end
		end
	end
	newPID = generatePointID(iterate(2).p);
	
	if wheresched_X(iterate(2).x,iterate(2).p)
		newCost = wheresched(iterate(2).x,iterate(2).p);
		disp(sprintf('initial point 2 id=%d, objective function value=%d',newPID,newCost));
	else
		disp('initial point 2 not feasible');
	end
end


return;


function result = isDataDecreasing(opInstId)
	opOutputSize = get(outputSize,opInstId);

	%disp(sprintf('opInstId=%s, opOutputSize=%d',opInstId,opOutputSize));
	children = get(opInstChildren, opInstId);
	sumChildSize = 0;
	numChildren=length(children);
	for j=1:1:numChildren
		childOpInstId = children{j};
		disp(sprintf('childOpInstId=%s',children{j}));
		childOutSize = get(outputSize,childOpInstId);
		disp(sprintf('childOutSize=%d',childOutSize));
		sumChildSize = sumChildSize + childOutSize;
	end
	disp(sprintf('* opInstId=%s, opOutputSize=%d, sumChildSize=%d\n',opInstId,opOutputSize,sumChildSize));

	if (opOutputSize < sumChildSize)
		disp(sprintf('opInstId %s is data decreasing\n',opInstId));
		result = 1;
	else
		result = 0;
	end
end

end