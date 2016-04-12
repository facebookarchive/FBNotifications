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

#import "FBNCardSize.h"
#import "FBNCardViewUtilities.h"

FBNCardSize FBNCardSizeFromString(NSString *_Nullable string) {
    if ([string isEqualToString:@"small"]) {
        return FBNCardSizeSmall;
    } else if ([string isEqualToString:@"medium"]) {
        return FBNCardSizeMedium;
    } else if ([string isEqualToString:@"large"]) {
        return FBNCardSizeLarge;
    }
    return FBNCardSizeInvalid;
}

CGSize FBNCardLayoutSizeThatFits(FBNCardSize cardSize, CGSize size) {
    const CGSize maxSize = CGSizeMake(400, 700);
    CGSize layoutSize = FBNSizeMin(maxSize, size);
    switch (cardSize) {
        case FBNCardSizeInvalid:
            return CGSizeZero;
        case FBNCardSizeSmall: {
            layoutSize.width *= 0.75;
            layoutSize.height *= 0.7;
        }
            break;
        case FBNCardSizeMedium: {
            layoutSize.width *= 0.83;
            layoutSize.height *= 0.9;
        }
            break;
        case FBNCardSizeLarge:
        default:
            break;
    }
    return FBNSizeAdjustToScreenScale(layoutSize, NSRoundPlain);
}
