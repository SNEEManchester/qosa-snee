function result = isLocSen(opInstId)
global locationConstraints;

	result = has_key(locationConstraints, opInstId);

end