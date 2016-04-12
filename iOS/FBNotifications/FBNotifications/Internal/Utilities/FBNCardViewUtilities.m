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

#include "FBNCardViewUtilities.h"

///--------------------------------------
#pragma mark - CGFloat
///--------------------------------------

CGFloat FBNCGFloatFromNumber(NSNumber *_Nullable number) {
#if CGFLOAT_IS_DOUBLE
    return number.doubleValue;
#else
    return number.floatValue;
#endif
}

CGFloat FBNCGFloatRound(CGFloat number, NSRoundingMode roundingMode) {
    switch (roundingMode) {
        case NSRoundPlain:
        case NSRoundBankers:
#if CGFLOAT_IS_DOUBLE
            number = round(number);
#else
            number = roundf(number);
#endif
        case NSRoundDown:
#if CGFLOAT_IS_DOUBLE
            number = floor(number);
#else
            number = floorf(number);
#endif
        case NSRoundUp:
#if CGFLOAT_IS_DOUBLE
            number = ceil(number);
#else
            number = ceilf(number);
#endif
        default: break;
    }
    return number;
}

///--------------------------------------
#pragma mark - CGRect
///--------------------------------------

CGRect FBNRectMakeWithOriginSize(CGPoint origin, CGSize size) {
    return CGRectMake(origin.x, origin.y, size.width, size.height);
}

CGRect FBNRectMakeWithSizeCenteredInRect(CGSize size, CGRect rect) {
    CGPoint center = CGPointMake(CGRectGetMidX(rect), CGRectGetMidY(rect));
    CGPoint origin = CGPointMake(center.x - size.width / 2.0f, center.y - size.height / 2.0f);
    rect = FBNRectMakeWithOriginSize(origin, size);
    return FBNRectAdjustToScreenScale(rect, NSRoundPlain);
}

///--------------------------------------
#pragma mark - CGSize
///--------------------------------------

CGSize FBNSizeMin(CGSize size1, CGSize size2) {
    CGSize size = CGSizeZero;
    size.width = (float)fmin(size1.width, size2.width);
    size.height = (float)fmin(size1.height, size2.height);
    return size;
}

CGSize FBNSizeMax(CGSize size1, CGSize size2) {
    CGSize size = CGSizeZero;
    size.width = (float)fmax(size1.width, size2.width);
    size.height = (float)fmax(size1.height, size2.height);
    return size;
}

CGFloat FBNAspectFillScaleThatFits(CGSize size, CGSize fitSize) {
    return MIN(fitSize.width / size.width, fitSize.height / size.height);
}

///--------------------------------------
#pragma mark - Screen Scaling
///--------------------------------------

CGRect FBNRectAdjustToScreenScale(CGRect rect, NSRoundingMode roundingMode) {
    rect.origin.x = FBNFloatAdjustToScreenScale(rect.origin.x, roundingMode);
    rect.origin.y = FBNFloatAdjustToScreenScale(rect.origin.y, roundingMode);
    rect.size = FBNSizeAdjustToScreenScale(rect.size, roundingMode);
    return rect;
}

CGSize FBNSizeAdjustToScreenScale(CGSize size, NSRoundingMode roundingMode) {
    size.width = FBNFloatAdjustToScreenScale(size.width, roundingMode);
    size.height = FBNFloatAdjustToScreenScale(size.height, roundingMode);
    return size;
}

CGFloat FBNFloatAdjustToScreenScale(CGFloat value, NSRoundingMode roundingMode) {
    const CGFloat scale = [UIScreen mainScreen].scale;
    value = value * scale;
    value = FBNCGFloatRound(value, roundingMode);
    value /= scale;
    return value;
}

UIViewController *_Nullable FBNApplicationTopMostViewController() {
    UIWindow *keyWindow = [UIApplication sharedApplication].keyWindow;
    UIViewController *viewController = keyWindow.rootViewController;
    while (viewController.presentedViewController && !viewController.presentedViewController.isBeingDismissed) {
        viewController = viewController.presentedViewController;
    }
    return viewController;
}
