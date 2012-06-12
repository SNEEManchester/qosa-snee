function pID = generatePointID(p)
global DAFidPidMap;
global pidDAFidMap;

	np  = length(p);
	tmpstr = '9';

	for k = 1:np
		tmpstr = strcat(tmpstr,num2str(p{k}),'_');
	end

	checksum = md5(tmpstr);
	%pID = hex2num(checksum);
	
	if has_key(DAFidPidMap, checksum)
		pID = get(DAFidPidMap, checksum);
	else
		pID = count(DAFidPidMap) + 1;
		disp('*****************');
		DAFidPidMap = put(DAFidPidMap, checksum, pID);
		
		pidDAFidMap{pID} = checksum;
	end
end