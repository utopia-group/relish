// constant terminal symbol
c

// variable terminal symbol
x1

// non-terminal symbol: maximum recursion depth  
e: 1

// productions 
// operator, arity, argument_1, .., argument_n, return, rank cost
Plus, 2, e, c, e, 1
Id, 1, x1, e, 1

// start symbol 
e

