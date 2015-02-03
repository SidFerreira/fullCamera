

#import "FZFullCamera.h"
#import "FZFullCameraViewController.h"

@implementation FZFullCamera


@synthesize namedImageForButtonBackImage;
@synthesize namedImageForButtonBackImageHighlighted;
@synthesize namedImageForButtonNextImage;
@synthesize namedImageForButtonNextImageHighlighted;
@synthesize namedImageForButtonFlashOnImage;
@synthesize namedImageForButtonFlashOnImageHighlighted;
@synthesize namedImageForButtonFlashOff;
@synthesize namedImageForButtonFlashOffHighlighted;
@synthesize namedImageForButtonSwitchCamera;
@synthesize namedImageForButtonSwitchCameraHighlighted;
@synthesize namedImageForButtonSourceGallery;
@synthesize namedImageForButtonSourceGalleryHighlighted;
@synthesize namedImageForButtonSourcePhoto;
@synthesize namedImageForButtonSourcePhotoHighlighted;
@synthesize namedImageForButtonSourcePhotoActive;
@synthesize namedImageForButtonSourcePhotoActiveHighlighted;
@synthesize namedImageForButtonSourceVideo;
@synthesize namedImageForButtonSourceVideoHighlighted;
@synthesize namedImageForButtonSourceVideoActive;
@synthesize namedImageForButtonSourceVideoActiveHighlighted;


@synthesize refViewController;

BOOL mayTakePicture = FALSE;
BOOL isPortrait = TRUE;
FZFullCameraViewController* cameraViewController;


- (UIViewController *) viewController {
    return refViewController;
}

- (void) setupNamedImages {
    self.namedImageForButtonBackImage = @"fullCamBack.png";
    self.namedImageForButtonBackImageHighlighted = @"fullCamBackHighlighted.png";
    
    self.namedImageForButtonNextImage = @"fullCamNext.png";
    self.namedImageForButtonNextImageHighlighted = @"fullCamNextHighlighted.png";
    
    self.namedImageForButtonFlashOnImage = @"fullCamFlashOn.png";
    self.namedImageForButtonFlashOnImageHighlighted = @"fullCamFlashOnHighlighted.png";
    
    self.namedImageForButtonFlashOff = @"fullCamFlashOff.png";
    self.namedImageForButtonFlashOffHighlighted = @"fullCamFlashOffHighlighted.png";
    
    self.namedImageForButtonSwitchCamera = @"fullCamSwitchCamera.png";
    self.namedImageForButtonSwitchCameraHighlighted = @"fullCamSwitchCameraHighlighted.png";
    
    self.namedImageForButtonSourceGallery = @"fullCamGallery.png";
    self.namedImageForButtonSourceGalleryHighlighted = @"fullCamGalleryHighlighted.png";
    
    self.namedImageForButtonSourcePhoto = @"fullCamPhoto.png";
    self.namedImageForButtonSourcePhotoHighlighted = @"fullCamPhotoHighlighted.png";
    self.namedImageForButtonSourcePhotoActive = @"fullCamPhotoActive.png";
    self.namedImageForButtonSourcePhotoActiveHighlighted = @"fullCamPhotoActiveHighlighted.png";
    
    self.namedImageForButtonSourceVideo = @"fullCamVideo.png";
    self.namedImageForButtonSourceVideoHighlighted = @"fullCamVideoHighlighted.png";
    self.namedImageForButtonSourceVideoActive = @"fullCamVideoActive.png";
    self.namedImageForButtonSourceVideoActiveHighlighted = @"fullCamVideoActiveHighlighted.png";
}

- (void)show {
    [self setupNamedImages];

    cameraViewController = [[FZFullCameraViewController alloc] init];
    cameraViewController.delegate = self;
    [[self viewController] presentViewController:cameraViewController animated:false completion:nil];
}

@end