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

#import "FBNCardDismissButton.h"

#import "FBNCardViewUtilities.h"

static const CGFloat FBNCardDismissButtonEdgeLength = 16.0;
static const CGFloat FBNCardDismissButtonLineWidth = 2.0;

@implementation FBNCardDismissButton

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

- (void)setImageColor:(UIColor *)imageColor {
    if (self.imageColor != imageColor) {
        _imageColor = imageColor;
        [self setImage:[[self class] _imageWithColor:imageColor] forState:UIControlStateNormal];
    }
}

///--------------------------------------
#pragma mark - UIView
///--------------------------------------

- (CGSize)sizeThatFits:(CGSize)boundingSize {
    CGSize size = CGSizeZero;
    size.width = MIN(FBNCardDismissButtonEdgeLength, boundingSize.width);
    size.height = MIN(FBNCardDismissButtonEdgeLength, boundingSize.height);
    return size;
}

- (BOOL)pointInside:(CGPoint)point withEvent:(UIEvent *)event {
    CGFloat edgeInset = CGRectGetWidth(self.bounds) - 44.0f;
    CGRect bigBounds = CGRectInset(self.bounds, edgeInset, edgeInset);
    return CGRectContainsPoint(bigBounds, point);
}

///--------------------------------------
#pragma mark - Default
///--------------------------------------

+ (UIImage *)_imageWithColor:(UIColor *)color {
    CGRect imageRect = FBNRectMakeWithOriginSize(CGPointZero, CGSizeMake(FBNCardDismissButtonEdgeLength, FBNCardDismissButtonEdgeLength));

    UIGraphicsBeginImageContextWithOptions(imageRect.size, NO, 0.0f);

    [color setStroke];

    UIBezierPath *path = [UIBezierPath bezierPath];

    [path moveToPoint:CGPointZero];
    [path addLineToPoint:CGPointMake(CGRectGetMaxX(imageRect), CGRectGetMaxY(imageRect))];

    [path moveToPoint:CGPointMake(CGRectGetMaxX(imageRect), CGRectGetMinY(imageRect))];
    [path addLineToPoint:CGPointMake(CGRectGetMinX(imageRect), CGRectGetMaxY(imageRect))];

    path.lineWidth = FBNCardDismissButtonLineWidth;

    [path stroke];

    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    return image;
}

@end
