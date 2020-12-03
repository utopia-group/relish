// constant terminal symbol
i
t
k
j
c

// variable terminal symbol
x1
x2

// non-terminal symbol: maximum recursion depth  
res: 4
comp: 0
intVal: 0 
strVal: 0 
pos: 0
x: 0

// productions 
// operator, arity, argument_1, .., argument_n, return, rank cost
Id, 1, comp, res, 1
Conditional, 2, comp, res, res, 1
IntLt, 2, intVal, intVal, comp, 1
StrLt, 2, strVal, strVal, comp, 1
CountChar, 2, x, c, intVal, 1
Len, 1, x, intVal, 1
ToInt, 1, strVal, 1
Id3, 1, x, strVal, 1
SubStr, 3, x, pos, pos, strVal, 1
StartPos, 3, x, t, k, pos, 1
EndPos, 3, x, t, k, pos, 1
End, 1, x, pos, 1
ConstPos, 1, j, pos, 1
Id1, 1, x1, x, 1
Id2, 1, x2, x, 1

// start symbol 
res

