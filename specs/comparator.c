#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

#define LEN 3
#define MAX_LEN 16

typedef struct stString {
  int length;
  unsigned char data[MAX_LEN];
} String;

String mkString(unsigned char* data, int len) {
    String str;
    str.length = len;
    memset(str.data, 0, MAX_LEN);
    for (int i = 0; i < len; ++i) {
        str.data[i] = data[i];
    }
    return str;
}

int StringEq(String s1, String s2) {
    int len = s1.length;
    if (len != s2.length) return 0;
    for (int i = 0; i < len; ++i) {
       if (s1.data[i] != s2.data[i]) return 0;
    }
    return 1;
}

int Id(int comp) {
  return comp;
}

int Conditional(int comp, int res) {
  if (comp == 0) return res; 
  return comp;
}

int IntLt(int intVal_1, int intVal_2) {
  if (intVal_1 == intVal_2) {
    return 0; 
  } else if (intVal_1 < intVal_2) {
    return 1;
  } else {
    return -1;
  }
}

int IntGt(int intVal_1, int intVal_2) {
  return -IntLt(intVal_1, intVal_2);
}

int compareString(String first, String second) {
  int i = 0; 
  for ( ; i < first.length && i < second.length && first.data[i] == second.data[i]; i ++) ; 
  if (i == first.length && i == second.length) {
    return 0;
  } else if (i == first.length) { // lt 
    return -1; 
  } else if (i == second.length) { // gt
    return 1;
  } else if (first.data[i] < second.data[i]) { // lt 
    return -1;
  } else if (first.data[i] > second.data[i]) { // gt 
    return 1;
  }
}

int StrLt(String strVal_1, String strVal_2) {
  int i = 0; 
  for ( ; i < strVal_1.length && i < strVal_2.length && strVal_1.data[i] == strVal_2.data[i]; i ++) ; 
  if (i == strVal_1.length && i == strVal_2.length) {
    return 0;
  } else if (i == strVal_1.length) { 
    return 1; 
  } else if (i == strVal_2.length) { 
    return -1;
  } else if (strVal_1.data[i] < strVal_2.data[i]) { 
    return 1;
  } else if (strVal_1.data[i] > strVal_2.data[i]) { 
    return -1;
  }
}

int StrGt(String strVal_1, String strVal_2) {
  return -StrLt(strVal_1, strVal_2); 
}

int CountChar(String x, unsigned char c) {
  int ret = 0; 
  for (int i = 0; i < x.length; i ++) {
    if (x.data[i] == c) ret ++;
  }
  return ret;
}

int Len(String x) {
  return x.length;
}

int ToInt(String strVal) {
  short ret = 0;
  for (int i = 0; i < strVal.length; i ++) {
    unsigned char c = strVal.data[i];
    // assume c is always a digit 
    ret = ret * 10 + (c - '0');
  }
  return ret;
}

bool MatchToken(int token, unsigned char c) {
  if (token == 0) { // digit
    if (c >= '0' && c <= '9') return true;
    return false;
  } else { // alphabet or digit
    if (   (c >= '0' && c <= '9')
        || (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z') ) 
      return true;
    return false;
  }
}

int nextState(int state, bool matched) {
  if (matched) return 1;
  return 0;
}

int StartPos(String x, int token, int k) {
  int cur = 0;
  int count = 0;
  for (int i = 0; i < x.length; i ++) {
    char c = x.data[i]; 
    bool matched = MatchToken(token, c);
    int next = nextState(cur, matched);
    if (cur == 0 && next == 1) {
      count ++;
      if (count == k) return i;
    }
    cur = next;
  }
  return 0;
}

int EndPos(String x, int token, int k) {
  int cur = 0;
  int count = 0;
  for (int i = 0; i < x.length; i ++) {
    char c = x.data[i]; 
    bool matched = MatchToken(token, c);
    int next = nextState(cur, matched);
    if (cur == 1 && next == 0) {
      count ++;
      if (count == k) return i;
    }
    cur = next;
  }
  if (cur == 1 && count == k - 1) return x.length; 
  return 0;
}

int End(String x) {
  return x.length;
}

int ConstPos(int j) {
  return j;
}

String SubStr(String x, int start, int end) {
  String ret; 
  memset(ret.data, 0, MAX_LEN); 
  for (int i = start; i < end; i ++) {
    ret.data[i - start] = x.data[i];
  }
  ret.length = end - start;
  return ret;
}

String Id1(String x1) {
  return x1;
}

String Id2(String x2) {
  return x2;
}

void PrintString(String str) {
    printf("Len: %d Data: ", str.length);
    for (int i = 0; i < str.length; ++i) {
        printf("%c", str.data[i]);
    }
    printf("\n");
}

