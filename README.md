# MODULA-2 Programming Language Syntax Check

## Description:
This project involves developing an LL(1) predictive parser for a subset of the MODULA-2 programming language.
The goal is to implement a parser in Java that can read, tokenize, and parse source code written in this subset of MODULA-2, ensuring that the code adheres to the given grammar rules.
The parser will use an LL(1) parsing table to guide the parsing process.

## Grammer Rules:
module-decl --> module-heading - declarations block name . 

module-heading --> module name ; 

block --> begin stmt-list end

declarations --> const-decl var-decl procedure-decl  

const-decl --> const const-list | lambda

const-list --> name = value ; const-list | lambda 

var-decl --> var var-list | lambda

var-list --> var-item ; var-list | lambda

var-item --> name-list : data-type

name-list --> name more-names 

more-names --> , name-list | lambda

data-type --> integer | real | char 

procedure-decl --> procedure-heading declarations block name ; procedure-decl | lambda

procedure-heading --> procedure name ; 

stmt-list --> statement ; stmt-list | lambda

statement --> ass-stmt | read-stmt | write-stmt | if-stmt | while-stmt | loop-stmt | exit-stmt | call-stmt | block | lambda

ass-stmt --> name := exp

exp --> term exp-prime

exp-prime --> add-oper term exp-prime | lambda	

term --> factor term-prime  

term-prime --> mul-oper factor term-prime | lambda

factor -->  ( exp ) | name-value

add-oper --> + | -  

mul-oper --> * | / | mod | div

read-stmt --> readint ( name-list ) | readreal ( name-list ) | readchar ( name-list ) | readln  

write-stmt --> writeint ( write-list ) | writereal ( write-list ) | writechar ( write-list ) | writeln  

write-list --> write-item more-write-value 

more-write-value --> , write-list | lambda

write-item --> name | value   

if-stmt --> if condition then stmt-list else-part end

else-part --> else stmt-list | lambda

while-stmt --> while condition do stmt-list end

loop-stmt --> loop stmt-list until condition   

exit-stmt --> exit      

call-stmt --> call name

condition --> name-value relational-oper name-value   

relational-oper --> = | |= | < | <= | > | >=

name-value --> name | value 

value --> integer-value | real-value

### Note: There is app_photo folder
