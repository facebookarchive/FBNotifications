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

#import "FBNCardColor.h"

UIColor *_Nullable FBNCardColorFromRGBAHex(NSString *string) {
    if (![string isKindOfClass:[NSString class]]) {
        return nil;
    }

    NSString *hexString = [string substringFromIndex:1];

    unsigned long long hexValue = 0;
    [[NSScanner scannerWithString:hexString] scanHexLongLong:&hexValue];

    CGFloat divisor = 255.0;
    CGFloat red   = ((hexValue & 0xFF000000) >> 24) / divisor;
    CGFloat green = ((hexValue & 0x00FF0000) >> 16) / divisor;
    CGFloat blue  = ((hexValue & 0x0000FF00) >> 8)  / divisor;
    CGFloat alpha = (hexValue & 0x000000FF)         / divisor;
    return [UIColor colorWithRed:red green:green blue:blue alpha:alpha];
}

FBNCardContrastColor FBNCardContrastColorForColor(UIColor *color) {
    CGFloat red = 0.0, green = 0.0, blue = 0.0;
    [color getRed:&red green:&green blue:&blue alpha:NULL];
    CGFloat perceptiveLuminance = (red * 255 * 299 + green * 255 * 587 + blue * 255 * 114) / 1000.0f;
    return (perceptiveLuminance >= 128 ? FBNCardContrastColorBlack : FBNCardContrastColorWhite);
}
