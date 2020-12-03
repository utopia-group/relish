#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#define LEN 2
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

void PrintString(String str) {
    printf("Len: %d Data: ", str.length);
    for (int i = 0; i < str.length; ++i) {
        printf("%c", str.data[i]);
    }
    printf("\n");
}

int StringEq(String s1, String s2) {
    int len = s1.length;
    if (len != s2.length) return 0;
    for (int i = 0; i < len; ++i) {
       if (s1.data[i] != s2.data[i]) return 0;
    }
    return 1;
}

String Id1(String str) {
    return str;
}

String Id2(String str) {
    return str;
}

String ReshapeRadix7(String str) {
    int len = str.length;
    int div = len / 7;
    int mod = len % 7;
    String ret;
    ret.length = (mod == 0) ? (8 * div) : (8 * div + mod + 1);
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; i += 7) {
        ret.data[j  ] = ((str.data[i  ]       ) >> 1);
        ret.data[j+1] = ((str.data[i  ] & 0x01) << 6) | ((str.data[i+1] & 0xfc) >> 2);
        ret.data[j+2] = ((str.data[i+1] & 0x03) << 5) | ((str.data[i+2] & 0xf8) >> 3);
        ret.data[j+3] = ((str.data[i+2] & 0x07) << 4) | ((str.data[i+3] & 0xf0) >> 4);
        ret.data[j+4] = ((str.data[i+3] & 0x0f) << 3) | ((str.data[i+4] & 0xe0) >> 5);
        ret.data[j+5] = ((str.data[i+4] & 0x1f) << 2) | ((str.data[i+5] & 0xc0) >> 6);
        ret.data[j+6] = ((str.data[i+5] & 0x3f) << 1) | ((str.data[i+6] & 0x80) >> 7);
        ret.data[j+7] = ((str.data[i+6] & 0x7f));
        j += 8;
    }
    return ret;
}

String ReshapeRadix6(String str) {
    int len = str.length;
    int div = len / 3;
    int mod = len % 3;
    String ret;
    ret.length = (mod == 0) ? (div * 4) : (div * 4 + mod + 1);
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; i += 3) {
        ret.data[j  ] = ((str.data[i  ]       ) >> 2);
        ret.data[j+1] = ((str.data[i  ] & 0x03) << 4) | (str.data[i+1] >> 4);
        ret.data[j+2] = ((str.data[i+1] & 0x0f) << 2) | (str.data[i+2] >> 6);
        ret.data[j+3] = ((str.data[i+2] & 0x3f));
        j += 4;
    }
    return ret;
}

String ReshapeRadix5(String str) {
    int len = str.length;
    int div = len / 5;
    int mod = len % 5;
    String ret;
    if (mod == 1) {
        ret.length = div * 8 + 2;
    } else if (mod == 2) {
        ret.length = div * 8 + 4;
    } else if (mod == 3) {
        ret.length = div * 8 + 5;
    } else if (mod == 4) {
        ret.length = div * 8 + 7;
    } else { // mod == 0
        ret.length = div * 8;
    }
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; i += 5) {
        ret.data[j  ] = ((str.data[i  ]       ) >> 3);
        ret.data[j+1] = ((str.data[i  ] & 0x07) << 2) | ((str.data[i+1]       ) >> 6);
        ret.data[j+2] = ((str.data[i+1] & 0x3e) >> 1);
        ret.data[j+3] = ((str.data[i+1] & 0x01) << 4) | ((str.data[i+2] & 0xf0) >> 4);
        ret.data[j+4] = ((str.data[i+2] & 0x0f) << 1) | ((str.data[i+3] & 0x80) >> 7);
        ret.data[j+5] = ((str.data[i+3] & 0x7c) >> 2);
        ret.data[j+6] = ((str.data[i+3] & 0x03) << 3) | ((str.data[i+4] & 0xe0) >> 5);
        ret.data[j+7] = ((str.data[i+4] & 0x1f));
        j += 8;
    }
    return ret;
}

String ReshapeRadix4(String str) {
    int len = str.length;
    String ret;
    ret.length = 2 * len;
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; ++i) {
        ret.data[j  ] = ((str.data[i]       ) >> 4);
        ret.data[j+1] = ((str.data[i] & 0x0f));
        j += 2;
    }
    return ret;
}

String ReshapeRadix3(String str) {
    int len = str.length;
    int div = len / 3;
    int mod = len % 3;
    String ret;
    if (mod == 1) {
        ret.length = 8 * div + 3;
    } else if (mod == 2) {
        ret.length = 8 * div + 6;
    } else { // mod == 0
        ret.length = 8 * div;
    }
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; i += 3) {
        ret.data[j  ] = ((str.data[i  ]       ) >> 5);
        ret.data[j+1] = ((str.data[i  ] & 0x1c) >> 2);
        ret.data[j+2] = ((str.data[i  ] & 0x03) << 1) | ((str.data[i+1] & 0x80) >> 7);
        ret.data[j+3] = ((str.data[i+1] & 0x70) >> 4);
        ret.data[j+4] = ((str.data[i+1] & 0x0e) >> 1);
        ret.data[j+5] = ((str.data[i+1] & 0x01) << 2) | ((str.data[i+2] & 0xc0) >> 6);
        ret.data[j+6] = ((str.data[i+2] & 0x38) >> 3);
        ret.data[j+7] = ((str.data[i+2] & 0x07));
        j += 8;
    }
    return ret;
}

String ReshapeRadix2(String str) {
    int len = str.length;
    String ret;
    ret.length = 4 * len;
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; ++i) {
        ret.data[j  ] = (str.data[i]       ) >> 6;
        ret.data[j+1] = (str.data[i] & 0x30) >> 4;
        ret.data[j+2] = (str.data[i] & 0x0c) >> 2;
        ret.data[j+3] = (str.data[i] & 0x03);
        j += 4;
    }
    return ret;
}

String Reshape(String str, int radix) {
    if (radix == 7) {
        return ReshapeRadix7(str);
    } else if (radix == 6) {
        return ReshapeRadix6(str);
    } else if (radix == 5) {
        return ReshapeRadix5(str);
    } else if (radix == 4) {
        return ReshapeRadix4(str);
    } else if (radix == 3) {
        return ReshapeRadix3(str);
    } else if (radix == 2) {
        return ReshapeRadix2(str);
    } else { // radix == 8
        return str;
    }
}

String PadToMultiple(String str, int num, unsigned char ch) {
    int len = str.length;
    int mod = len % num;
    if (mod == 0) return str;
    int newLen = len + num - mod;
    String ret;
    ret.length = newLen;
    for (int i = 0; i < len; ++i) {
        ret.data[i] = str.data[i];
    }
    for (int i = len; i < newLen; ++i) {
        ret.data[i] = ch;
    }
    return ret;
}

String Enc64(String x1) {
    int len = x1.length;
    String ret;
    ret.length = len;
    for (int i = 0; i < len; ++i) {
        unsigned char b = x1.data[i];
        if (b <= 0x19) {
            ret.data[i] = (b + 0x41);
        } else if (b <= 0x33) {
            ret.data[i] = (b + 0x47);
        } else if (b <= 0x3d) {
            ret.data[i] = (b - 0x04);
        } else if (b == 0x3e) {
            ret.data[i] = 0x2b;
        } else {
            ret.data[i] = 0x2f;
        }
    }
    return ret;
}

String Dec64(String x1) {
    int len = x1.length;
    String ret;
    ret.length = len;
    for (int i = 0; i < len; ++i) {
        unsigned char b = x1.data[i];
        if (b >= 0x41 && b <= 0x5a) {
            ret.data[i] = (b - 0x41);
        } else if (b >= 0x61 && b <= 0x7a) {
            ret.data[i] = (b - 0x47);
        } else if (b >= 0x30 && b <= 0x39) {
            ret.data[i] = (b + 0x4);
        } else if (b == 0x2b) {
            ret.data[i] = 0x3e;
        } else {
            ret.data[i] = 0x3f;
        }
    }
    return ret;
}

String Enc32(String x1) {
    int len = x1.length;
    String ret;
    ret.length = len;
    for (int i = 0; i < len; ++i) {
        unsigned char b = x1.data[i];
        if (b <= 0x19) {
            ret.data[i] = (b + 0x41);
        } else {
            ret.data[i] = (b + 0x18);
        }
    }
    return ret;
}

String Dec32(String x1) {
    int len = x1.length;
    String ret;
    ret.length = len;
    for (int i = 0; i < len; ++i) {
        unsigned char b = x1.data[i];
        if (b >= 0x41 && b <= 0x5a) {
            ret.data[i] = (b - 0x41);
        } else {
            ret.data[i] = (b - 0x18);
        }
    }
    return ret;
}

String Enc16(String x1) {
    int len = x1.length;
    String ret;
    ret.length = len;
    for (int i = 0; i < len; ++i) {
        unsigned char b = x1.data[i];
        if (b <= 0x9) {
            ret.data[i] = (b + 0x30);
        } else {
            ret.data[i] = (b + 0x37);
        }
    }
    return ret;
}

String Dec16(String x1) {
    int len = x1.length;
    String ret;
    ret.length = len;
    for (int i = 0; i < len; ++i) {
        unsigned char b = x1.data[i];
        if (b >= 0x30 && b <= 0x39) {
            ret.data[i] = (b - 0x30);
        } else {
            ret.data[i] = (b - 0x37);
        }
    }
    return ret;
}

String RemovePad(String str, unsigned char ch) {
    int len = str.length;
    String ret;
    ret.length = len;
    for (int i = 0; i < len; ++i) {
        if (str.data[i] == ch) {
            ret.length = i;
            break;
        } else {
            ret.data[i] = str.data[i];
        }
    }
    return ret;
}

String LSBReshapeRadix7(String str) {
    int len = str.length;
    int div = len / 8;
    int mod = len % 8;
    String ret;
    ret.length = (mod == 0) ? (7 * div) : (7 * div + mod - 1);
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; ++i) {
        ret.data[j  ] = ((str.data[i  ] & 0x7f) << 1) | ((str.data[i+1] & 0x40) >> 6);
        ret.data[j+1] = ((str.data[i+1] & 0x3f) << 2) | ((str.data[i+2] & 0x60) >> 5);
        ret.data[j+2] = ((str.data[i+2] & 0x1f) << 3) | ((str.data[i+3] & 0x70) >> 4);
        ret.data[j+3] = ((str.data[i+3] & 0x0f) << 4) | ((str.data[i+4] & 0x78) >> 3);
        ret.data[j+4] = ((str.data[i+4] & 0x07) << 5) | ((str.data[i+5] & 0x7c) >> 2);
        ret.data[j+5] = ((str.data[i+5] & 0x03) << 6) | ((str.data[i+6] & 0x7e) >> 1);
        ret.data[j+6] = ((str.data[i+6] & 0x01) << 7) | ((str.data[i+7] & 0x7f));
        j += 7;
    }
    return ret;
}

String LSBReshapeRadix6(String str) {
    int len = str.length;
    int div = len / 4;
    int mod = len % 4; // mod \in {0, 2, 3}
    String ret;
    ret.length = (mod == 0) ? (div * 3) : (div * 3 + mod - 1);
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; i += 4) {
        ret.data[j  ] = (str.data[i] << 2) | ((str.data[i+1] & 0x30) >> 4);
        ret.data[j+1] = ((str.data[i+1] & 0x0f) << 4) | ((str.data[i+2] & 0x3c) >> 2);
        ret.data[j+2] = ((str.data[i+2] & 0x03) << 6) | str.data[i+3];
        j += 3;
    }
    return ret;
}

String LSBReshapeRadix5(String str) {
    int len = str.length;
    int div = len / 8;
    int mod = len % 8;
    String ret;
    if (mod == 2) {
        ret.length = div * 5 + 1;
    } else if (mod == 4) {
        ret.length = div * 5 + 2;
    } else if (mod == 5) {
        ret.length = div * 5 + 3;
    } else if (mod == 7) {
        ret.length = div * 5 + 4;
    } else { // mod == 0
        ret.length = div * 5;
    }
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; i += 8) {
        ret.data[j  ] = ((str.data[i  ]       ) << 3) | ((str.data[i+1] & 0x1c) >> 2);
        ret.data[j+1] = ((str.data[i+1] & 0x03) << 6) | ((str.data[i+2] & 0x1f) << 1) | ((str.data[i+3] & 0x10) >> 4);
        ret.data[j+2] = ((str.data[i+3] & 0x0f) << 4) | ((str.data[i+4] & 0x1e) >> 1);
        ret.data[j+3] = ((str.data[i+4] & 0x01) << 7) | ((str.data[i+5] & 0x1f) << 2) | ((str.data[i+6] & 0x18) >> 3);
        ret.data[j+4] = ((str.data[i+6] & 0x07) << 5) | ((str.data[i+7] & 0x1f));
        j += 5;
    }
    return ret;
}

String LSBReshapeRadix4(String str) {
    int len = str.length;
    String ret;
    ret.length = len / 2;
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; i += 2) {
        ret.data[j++] = (str.data[i] << 4) | (str.data[i+1] & 0x0f);
    }
    return ret;
}

String LSBReshapeRadix3(String str) {
    int len = str.length;
    int div = len / 8;
    int mod = len % 8;
    String ret;
    if (mod == 3) {
        ret.length = 3 * div + 1;
    } else if (mod == 6) {
        ret.length = 3 * div + 2;
    } else { // mod == 0
        ret.length = 3 * div;
    }
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; i += 8) {
        ret.data[j  ] = ((str.data[i  ]       ) << 5) | ((str.data[i+1] & 0x07) << 2) | ((str.data[i+2] & 0x06) >> 1);
        ret.data[j+1] = ((str.data[i+2] & 0x01) << 7) | ((str.data[i+3] & 0x07) << 4) | ((str.data[i+4] & 0x07) << 1) | ((str.data[i+5] & 0x04) >> 2);
        ret.data[j+2] = ((str.data[i+5] & 0x03) << 6) | ((str.data[i+6] & 0x07) << 3) | ((str.data[i+7] & 0x07));
        j += 3;
    }
    return ret;
}

String LSBReshapeRadix2(String str) {
    int len = str.length;
    String ret;
    ret.length = len / 4;
    memset(ret.data, 0, MAX_LEN);
    int j = 0;
    for (int i = 0; i < len; i += 4) {
        ret.data[j++] = ((str.data[i  ]       ) << 6) | ((str.data[i+1] & 0x03) << 4) |
                        ((str.data[i+2] & 0x03) << 2) | ((str.data[i+3] & 0x03));
    }
    return ret;
}

String LSBReshape(String str, int radix) {
    if (radix == 7) {
        return LSBReshapeRadix7(str);
    } else if (radix == 6) {
        return LSBReshapeRadix6(str);
    } else if (radix == 5) {
        return LSBReshapeRadix5(str);
    } else if (radix == 4) {
        return LSBReshapeRadix4(str);
    } else if (radix == 3) {
        return LSBReshapeRadix3(str);
    } else if (radix == 2) {
        return LSBReshapeRadix2(str);
    } else { // radix == 8
        return str;
    }
}

