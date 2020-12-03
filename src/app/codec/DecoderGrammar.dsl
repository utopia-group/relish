// constant terminal symbol
num
ch

// variable terminal symbol
x1

// non-terminal symbol: maximum recursion depth  
C: 2
D: 1
B: 2

// productions 
// operator, arity, argument_1, .., argument_n, return, rank cost
Id1, 1, x1, C, 1
RemovePad, 2, C, ch, C, 1
Dec64, 1, C, D, 1
Dec32, 1, C, D, 1
Dec16, 1, C, D, 1
Id2, 1, D, B, 1
LSBReshape, 2, B, num, B, 1

// start symbol 
B

