// Copyright (c) 2016-present, Facebook, Inc. All rights reserved.
//
// You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
// copy, modify, and distribute this software in source code or binary form for use
// in connection with the web services and APIs provided by Facebook.
//
// As with any software that integrates with the Facebook platform, your use of
// this software is subject to the Facebook Developer Principles and Policies
// [http://developers.facebook.com/policy/]. This copyright notice shall be
// included in all copies or substantial portions of the software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
// IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

#import "FBNCardHash.h"

#import <CommonCrypto/CommonDigest.h>

NSString *FBMD5HashFromData(NSData *data) {
    unsigned char md[CC_MD5_DIGEST_LENGTH];

    // NOTE: `__block` variables of a struct type seem to be bugged. The compiler emits instructions to read
    // from the stack past where they're supposed to exist. This fixes that, by only using a traditional pointer.
    CC_MD5_CTX ctx_val = { 0 };
    CC_MD5_CTX *ctx_ptr = &ctx_val;
    CC_MD5_Init(ctx_ptr);
    [data enumerateByteRangesUsingBlock:^(const void *bytes, NSRange byteRange, BOOL *stop) {
        CC_MD5_Update(ctx_ptr , bytes, (CC_LONG)byteRange.length);
    }];
    CC_MD5_Final(md, ctx_ptr);

    NSString *string = [NSString stringWithFormat:@"%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
                        md[0], md[1],
                        md[2], md[3],
                        md[4], md[5],
                        md[6], md[7],
                        md[8], md[9],
                        md[10], md[11],
                        md[12], md[13],
                        md[14], md[15]];
    return string;
}

NSString *FBMD5HashFromString(NSString *string) {
    return FBMD5HashFromData([string dataUsingEncoding:NSUTF8StringEncoding]);
}
