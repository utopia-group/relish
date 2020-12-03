// constant terminal symbol
a

// variable terminal symbol
x1

// non-terminal symbol: maximum recursion depth  
t: 1

// productions 
// operator, arity, argument_1, .., argument_n, return, rank cost
Multiply, 2, t, a, t, 1
Id, 1, x1, t, 1

// start symbol 
t

