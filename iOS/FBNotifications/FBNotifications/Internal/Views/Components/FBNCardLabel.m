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

#import "FBNCardLabel.h"

#import "FBNCardTextContent.h"
#import "FBNCardViewUtilities.h"

@implementation FBNCardLabel

///--------------------------------------
#pragma mark - Init
///--------------------------------------

+ (instancetype)labelFromTextContent:(FBNCardTextContent *)content {
    FBNCardLabel *label = [[self alloc] initWithFrame:CGRectZero];
    label.textColor = content.textColor;
    label.textAlignment = content.textAlignment;
    label.attributedText = content.attributedText;
    label.numberOfLines = 0;
    label.lineBreakMode = NSLineBreakByWordWrapping;
    return label;
}

///--------------------------------------
#pragma mark - UIView
///--------------------------------------

- (CGSize)sizeThatFits:(CGSize)size {
    // This is required to accommodate for UILabel behavior, where it returns size bigger than the fit size.
    return FBNSizeMin([super sizeThatFits:size], size);
}

@end
