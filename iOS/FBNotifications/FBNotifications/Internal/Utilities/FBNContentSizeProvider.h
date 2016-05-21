//
//  FBNContentSizeProvider.h
//  FBNotifications
//
//  Created by Nikita Lutsenko on 5/20/16.
//  Copyright © 2016 Facebook Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@protocol FBNContentSizeProvider <NSObject>

- (CGSize)contentSizeThatFitsParentContainerSize:(CGSize)fitSize;

@end
