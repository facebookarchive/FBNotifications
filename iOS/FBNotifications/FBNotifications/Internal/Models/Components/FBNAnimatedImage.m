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

#import "FBNAnimatedImage.h"

#import <ImageIO/ImageIO.h>
#import <MobileCoreServices/MobileCoreServices.h>

extern BOOL FBNImageSourceContainsAnimatedGIF(CGImageSourceRef source);
extern UIImage *FBNAnimatedImageFromImageSource(CGImageSourceRef source);
extern NSTimeInterval FBNImageSourceGetGIFFrameDelay(CGImageSourceRef imageSource, NSUInteger index);

///--------------------------------------
#pragma mark - Public
///--------------------------------------

UIImage *FBNAnimatedImageFromData(NSData *data) {
    if (!data) {
        return nil;
    }

    CGImageSourceRef source = CGImageSourceCreateWithData((__bridge CFDataRef)(data), NULL);

    UIImage *image = nil;
    if (FBNImageSourceContainsAnimatedGIF(source)) {
        image = FBNAnimatedImageFromImageSource(source);
    } else {
        image = [UIImage imageWithData:data];
    }

    if (source) {
        CFRelease(source);
    }

    return image;
}

///--------------------------------------
#pragma mark - Private
///--------------------------------------

BOOL FBNImageSourceContainsAnimatedGIF(CGImageSourceRef source) {
    return (source && UTTypeConformsTo(CGImageSourceGetType(source), kUTTypeGIF) && CGImageSourceGetCount(source) > 1);
}

UIImage *FBNAnimatedImageFromImageSource(CGImageSourceRef source) {
    CFRetain(source);
    NSUInteger numberOfFrames = CGImageSourceGetCount(source);
    NSMutableArray<UIImage *> *images = [NSMutableArray arrayWithCapacity:numberOfFrames];
    NSTimeInterval duration = 0.0;
    for (NSUInteger i = 0; i < numberOfFrames; ++i) {
        CGImageRef image = CGImageSourceCreateImageAtIndex(source, i, NULL);
        if (image) {
            UIImage *frameImage = [UIImage imageWithCGImage:image scale:1.0 orientation:UIImageOrientationUp];
            [images addObject:frameImage];
            CFRelease(image);
        } else {
            continue;
        }

        duration += FBNImageSourceGetGIFFrameDelay(source, i);
    }
    CFRelease(source);

    return [UIImage animatedImageWithImages:images duration:duration];
}

NSTimeInterval FBNImageSourceGetGIFFrameDelay(CGImageSourceRef source, NSUInteger index) {
    NSTimeInterval frameDelay = 0;
    CFDictionaryRef imageProperties = CGImageSourceCopyPropertiesAtIndex(source, index, NULL);
    if (!imageProperties) {
        return frameDelay;
    }

    CFDictionaryRef gifProperties = nil;
    if (CFDictionaryGetValueIfPresent(imageProperties, kCGImagePropertyGIFDictionary, (const void **)&gifProperties)) {
        const void *durationValue = nil;
        if (CFDictionaryGetValueIfPresent(gifProperties, kCGImagePropertyGIFUnclampedDelayTime, &durationValue)) {
            frameDelay = [(__bridge NSNumber *)durationValue doubleValue];
            if (frameDelay <= 0) {
                if (CFDictionaryGetValueIfPresent(gifProperties, kCGImagePropertyGIFDelayTime, &durationValue)) {
                    frameDelay = [(__bridge NSNumber *)durationValue doubleValue];
                }
            }
        }
    }
    CFRelease(imageProperties);

    return frameDelay;
}
