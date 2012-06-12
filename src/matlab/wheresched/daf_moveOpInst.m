function N = daf_moveOpInst(N, candNum, opInstIndex, opInstId, currentSite, newSite)

global operatorinstances;
global locationConstraints;
global rt;
global opInstDeepestConfSite;
global opInstChildren;
global opInstanceIndex;
global parentOpInst;
global numNeighbours
global sites;

	%Assign the operator instance to the new site
	N(candNum).p{opInstIndex} = newSite;

	disp(sprintf('Moving opInst %s from site %d to site %d\n',opInstId,currentSite,newSite));

	%Now shift the nearby operator instances if necessary
	%If newSite is a deeperSite in the routing tree, child operator instances may be need to be pushed down
	if rtDelta(currentSite, newSite) < 0
		confluencePath = getPath(currentSite, newSite);
		pushChildrenDown(opInstId, newSite, confluencePath);

	%If newSite is a shallower site in the routing tree, parent operator instances may need to be pulled up
	elseif rtDelta(currentSite, newSite) > 0
		pullParentsUp(opInstId, newSite);            
	end


return;

function [] = pushChildrenDown(opInst, newSite, cPath)
    
    children = get(opInstChildren, opInst);
    numChildren = length(children);
    for j=1:1:numChildren
        childOpInst = children{j};
        childIndex = get(opInstanceIndex, childOpInst);        
        childSite = N(candNum).p{childIndex};
        
        if rtDelta(childSite, newSite) < 0
            if inList(cPath, childSite)==1
                %need to check if childSite is on the path between currentSite and newSite
            
                if has_key(locationConstraints, childOpInst)
                    disp(sprintf('...   Attempt to move location sensitive child down: opInst %s', childOpInst)); 
                else
              	    disp(sprintf('...   Shifting child down: opInst %s from site %d to site %d',childOpInst,childSite,newSite));
            	    N(candNum).p{childIndex} = newSite;
                end
                pushChildrenDown(childOpInst, newSite, cPath);
            end
        end
    end    
end    
    
function path = getPath(source, dest)
	path = find_path(rt,source+1,dest+1); %+1 adjustment
	n = length(path);
	for j=1:1:n
		path(j) = path(j) - 1;
	end
end

function found = inList(list, element)
	n = length(list);
	found = 0;
	for j=1:1:n
		if list(j)==element
			found = 1;
		end
	end
end


function [] = pullParentsUp(opInst, newSite)
    
    if has_key(parentOpInst, opInst)
        paOpInst = get(parentOpInst, opInst);
        paIndex = get(opInstanceIndex, paOpInst);
        paSite = N(candNum).p{paIndex};
        
        if rtDelta(paSite, newSite) > 0
            disp(sprintf('...   Shifting parent up: opInst %s from site %d to site %d',paOpInst,paSite,newSite));        
            N(candNum).p{paIndex} = newSite;
            pullParentsUp(paOpInst, newSite);
        end
    end
end

%Compares the difference in site level in the routing tree.  The level for
%a given site is defined as the length of the path from the site to the
%sink.
%
%Returns zero if both sites are at the same level in the routing tree.
%Returns a negative number if the second site is at a deeper level in 
%the routing tree.
%Returns a positive number if the second site is at a shallower level in 
%the routing tree.
function d = rtDelta(firstSite, secondSite)

	firstSitePath = find_path(rt, firstSite+1, 0+1); %+1 adjustment
	firstSitePathLength = length(firstSitePath);
	
	secondSitePath = find_path(rt, secondSite+1, 0+1); %+1 adjustment
	secondSitePathLength = length(secondSitePath);
	
	d = firstSitePathLength - secondSitePathLength;
end

end