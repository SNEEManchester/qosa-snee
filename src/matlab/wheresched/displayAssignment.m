function [] = displayAssignment(p)
global operatorinstances;

	for i=1:1:length(operatorinstances)
		disp(sprintf('%s=%g',operatorinstances{i},p{i}));
	end

end