grammar StateCondition;

stat : expr EOF ;

expr : '(' expr ')'             # Paren
     | job=ID IS state=ID       # Is
     | NOT expr                 # Not
     | expr AND expr            # And
     | expr OR expr             # Or
    ;

IS : I S ;
NOT : N O T ;
AND : A N D ;
OR : O R ;  

ID : [A-Za-z0-9_\-.,@]+ ;

WS : [ \t\r\n]+ ->skip ; // skip spaces, tabs, newlines

fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');
