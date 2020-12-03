// constant terminal symbol
num
ch

// variable terminal symbol
x1

// non-terminal symbol: maximum recursion depth  
B: 2
E: 1
C: 2

// productions 
// operator, arity, argument_1, .., argument_n, return, rank cost
Id1, 1, x1, B, 1
Reshape, 2, B, num, B, 1
Enc64, 1, B, E, 1
Enc32, 1, B, E, 1
Enc16, 1, B, E, 1
Id2, 1, E, C, 1
PadToMultiple, 3, C, num, ch, C, 1

// start symbol 
C

