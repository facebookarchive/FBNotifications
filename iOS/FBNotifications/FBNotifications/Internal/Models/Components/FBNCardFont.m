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

#import "FBNCardFont.h"

NS_ASSUME_NONNULL_BEGIN

NSString *const FBNCardFontNameSystemRegular = @"system-regular";
NSString *const FBNCardFontNameSystemLight = @"system-light";
NSString *const FBNCardFontNameSystemItalic = @"system-italic";
NSString *const FBNCardFontNameSystemBold = @"system-bold";
NSString *const FBNCardFontNameSystemBoldItalic = @"system-bolditalic";

UIFont *FBNCardFontWithNameSize(NSString *_Nullable fontName, CGFloat size) {
    // Make sure we do case-insensitive comparison.
    fontName = fontName.lowercaseString;

    UIFont *systemFont = [UIFont systemFontOfSize:size];
    UIFontDescriptor *fontDescriptor = nil;
    if ([fontName isEqualToString:FBNCardFontNameSystemLight]) {
        fontDescriptor = [UIFontDescriptor fontDescriptorWithFontAttributes:@{ UIFontDescriptorFamilyAttribute: systemFont.familyName,
                                                                               UIFontDescriptorFaceAttribute : @"Light" }];
    } else if ([fontName isEqualToString:FBNCardFontNameSystemItalic]) {
        fontDescriptor = [systemFont.fontDescriptor fontDescriptorWithSymbolicTraits:UIFontDescriptorTraitItalic];
    } else if ([fontName isEqualToString:FBNCardFontNameSystemBold]) {
        fontDescriptor = [systemFont.fontDescriptor fontDescriptorWithSymbolicTraits:UIFontDescriptorTraitBold];
    } else if ([fontName isEqualToString:FBNCardFontNameSystemBoldItalic]) {
        fontDescriptor = [systemFont.fontDescriptor fontDescriptorWithSymbolicTraits:(UIFontDescriptorTraitBold | UIFontDescriptorTraitItalic)];
    } else {
        fontDescriptor = systemFont.fontDescriptor;
    }
    return [UIFont fontWithDescriptor:fontDescriptor size:size];
}

NS_ASSUME_NONNULL_END
