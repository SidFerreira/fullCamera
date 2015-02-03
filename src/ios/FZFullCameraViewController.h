//
//  FZFullCameraViewController.h
//  FullCam
//
//  Created by Sidney Ferreira on 09/01/15.
//  Copyright (c) 2015 FerreiraZ. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import "iCarousel.h"
#import "CaptureSessionManager.h"
#import "ELCImagePickerHeader.h"

@protocol FZFullCameraViewControllerDelegate <NSObject>

@property(nonatomic, retain) NSString*   namedImageForButtonBackImage;
@property(nonatomic, retain) NSString*   namedImageForButtonBackImageHighlighted;
@property(nonatomic, retain) NSString*   namedImageForButtonNextImage;
@property(nonatomic, retain) NSString*   namedImageForButtonNextImageHighlighted;
@property(nonatomic, retain) NSString*   namedImageForButtonFlashOnImage;
@property(nonatomic, retain) NSString*   namedImageForButtonFlashOnImageHighlighted;
@property(nonatomic, retain) NSString*   namedImageForButtonFlashOff;
@property(nonatomic, retain) NSString*   namedImageForButtonFlashOffHighlighted;
@property(nonatomic, retain) NSString*   namedImageForButtonSwitchCamera;
@property(nonatomic, retain) NSString*   namedImageForButtonSwitchCameraHighlighted;

@property(nonatomic, retain) NSString*   namedImageForButtonSourceGallery;
@property(nonatomic, retain) NSString*   namedImageForButtonSourceGalleryHighlighted;

@property(nonatomic, retain) NSString*   namedImageForButtonSourcePhoto;
@property(nonatomic, retain) NSString*   namedImageForButtonSourcePhotoHighlighted;
@property(nonatomic, retain) NSString*   namedImageForButtonSourcePhotoActive;
@property(nonatomic, retain) NSString*   namedImageForButtonSourcePhotoActiveHighlighted;

@property(nonatomic, retain) NSString*   namedImageForButtonSourceVideo;
@property(nonatomic, retain) NSString*   namedImageForButtonSourceVideoHighlighted;
@property(nonatomic, retain) NSString*   namedImageForButtonSourceVideoActive;
@property(nonatomic, retain) NSString*   namedImageForButtonSourceVideoActiveHighlighted;


@optional

- (void)cancelled;

@end

@interface FZFullCameraViewController : UIViewController <iCarouselDelegate, iCarouselDataSource, ELCImagePickerControllerDelegate>

@property (nonatomic, weak) IBOutlet id<FZFullCameraViewControllerDelegate> delegate;

@property(nonatomic) NSInteger           quality;
@property(nonatomic) NSInteger           maxPhotos;
@property(nonatomic) NSInteger           maxVideoTime;
@property(nonatomic) bool                saveToDevice;
@property(nonatomic) bool                shouldMergeVideos;
@property(nonatomic) bool                shouldUseFrontCamera;
@property(nonatomic) bool                allowedSource;
@property(nonatomic, retain) NSString*   textMaxPhotos;
@property(nonatomic, retain) NSString*   textDeletePhoto;
@property(nonatomic, retain) NSString*   textTrashPhotos;
@property(nonatomic, retain) NSString*   textMaxVideo;
@property(nonatomic, retain) NSString*   textTrashVideo;

//Capture
@property (retain) CaptureSessionManager *captureManager;
- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)contextInfo;

@end

