//
//  FZFullCamera.h
//  FullCam
//
//  Created by Sidney Ferreira on 07/01/15.
//  Copyright (c) 2015 FerreiraZ. All rights reserved.
//

#import <UIKit/UIKit.h>
//#import "CaptureManager.h"
#import "Foundation/Foundation.h"
//#import <Cordova/CDVPlugin.h>
#import "FZFullCameraViewController.h"

//CDVPlugin <UINavigationControllerDelegate>
@interface FZFullCamera : NSObject <FZFullCameraViewControllerDelegate>

@property (nonatomic, retain) UIViewController  *refViewController;

- (void)show;

@end

