%*********************************************************************
% experiment_Omega:  User-supplied function for defining Omega, based on p.
% --------------------------------------------------------------------
%   Variables:
%     A     = Coefficient matrix for bound and linear constraints
%     l     = Lower bounds for A*x for any iterate x
%     u     = Upper bounds for A*x for any iterate x
%     plist = list of possible values of categorical variable
%*********************************************************************
%n is number of continous variables.
%p is cell array of categorical variables.
function [A,l,u,plist] = wheresched_Omega(n,p)

global operatorinstances;
global sites;

disp('*** Starting Omega Function***')

for i=1:1:length(p)
   plist{i} = sites; % list of possible categorical values.    
end

%compulsary
A = 1;% 1 continous variable.   %this caused trouble initially, 1 works.
l = [1];
u = [1];
return
