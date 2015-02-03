//
//  FZFullCameraViewController.m
//  FullCam
//
//  Created by Sidney Ferreira on 09/01/15.
//  Copyright (c) 2015 FerreiraZ. All rights reserved.
//

#import "FZFullCameraViewController.h"

@interface FZFullCameraViewController ()

@end

@implementation FZFullCameraViewController

@synthesize captureManager;

const double TAG_SOURCE_GALLERY_VIEW   = 100;
const double TAG_SOURCE_GALLERY_BUTTON = 101;

const double TAG_SOURCE_PHOTO_VIEW     = 201;
const double TAG_SOURCE_PHOTO_NORMAL   = 202;
const double TAG_SOURCE_PHOTO_ACTIVE   = 203;

const double TAG_SOURCE_VIDEO_VIEW     = 300;
const double TAG_SOURCE_VIDEO_NORMAL   = 301;
const double TAG_SOURCE_VIDEO_ACTIVE   = 302;

const double TAG_CAROUSEL_PHOTOS       = 50;
const double TAG_CAROUSEL_SOURCES      = 51;

//UI Components
UIButton*                   buttonSwitchCamera;
UIButton*                   buttonBack;
UIButton*                   buttonNext;
UIButton*                   buttonFlashOn;
UIButton*                   buttonFlashOff;
UIImageView*                buttonSourceGallery;
UIImageView*                buttonSourcePhoto;
UIButton*                   buttonSourcePhotoActive;
UIImageView*                buttonSourceVideo;
UIButton*                   buttonSourceVideoActive;

UIView*                     progressGray;
UIView*                     progressRed;

//Source Views
UIView*                     headerBar;
UIView*                     footerBar;
UIView*                     bodyBar;

UIView*                     previewLayer;

CGRect                      screenSizedRect;

iCarousel*                  carouselPhotos;
NSMutableArray*             itemsPhotos;
NSString*                   itemMovie;

iCarousel*                  carouselSources;
NSMutableArray*             itemsSources;
ELCImagePickerController*   elcPicker;


bool         useFrontCamera = NO;
bool         isUsingFrontCamera = NO;
bool         shouldMergeVideos = YES;


@synthesize quality;
@synthesize maxPhotos;
@synthesize maxVideoTime;
@synthesize saveToDevice;
@synthesize shouldMergeVideos;
@synthesize shouldUseFrontCamera;
@synthesize allowedSource;
@synthesize textMaxPhotos;
@synthesize textDeletePhoto;
@synthesize textTrashPhotos;
@synthesize textMaxVideo;
@synthesize textTrashVideo;

/*
 quality: Quality of the saved image, expressed as a range of 0-100, where 100 is typically full resolution with no loss from file compression. The default is 50. (Number)
 saveToDevice : is the image saved also to the device or only returned to the app’s handler. Default to false. (Boolean)
 allowedSource : object containing between 1 to 3 elements, that can be “photo”, “video”, “gallery”. Used to define what source would be available when the camera is launched (other options would be greyed / not displayed). Default to all 3 elements. (Object)
 maxPhotos : max number of pics to take before asking the user to upload. Default to 5. (Number)
 maxPhotosText : the message that appears when the user try to take more than the limited number of pictures. Default : “You have reached the limit number of photos !”. (String)
 deletePhotoText : the message that appears when the user clicks on one of the thumbnails in order to delete a picture. Default : “Are you sure that you want to delete this photo ?”. (String)
 moveFromPhotosText : the message that appears when the user tries to switch out from photo mode and has pictures waiting for upload. Default : “Are you sure that you want to move away and lose the pictures you took ?”. (String)
 maxVideoTime : max time (in seconds) a user can use the video mode before upload. Default to 30. (Number)
 maxVideoText : the message that appears when maxVideoTime is reached. Default : “Maximum video length reached !”. (String)
 moveFromVideosText : the message that appears when the user tries to switch out from video mode and has a video waiting for upload. Default : “Are you sure that you want to move away and lose the video you took ?”. (String)

 */

- (void)viewDidLoad {
    [super viewDidLoad];
    
    quality = 50;
    maxPhotos = 5;
    maxVideoTime = 30;
    saveToDevice = false;
    shouldMergeVideos = YES;
    shouldUseFrontCamera = YES;
    allowedSource = false;
    textMaxPhotos   = @"You have reached the limit number of photos!";
    textDeletePhoto = @"Are you sure that you want to delete this photo?";
    textTrashPhotos = @"Are you sure that you want to move away and lose the pictures you took?";
    textMaxVideo    = @"Maximum video length reached!";
    textTrashVideo  = @"Are you sure that you want to move away and lose the video you took?";
    
    // Do any additional setup after loading the view.
    itemsPhotos  = [[NSMutableArray alloc] init];
    itemsSources = [[NSMutableArray alloc] init];
    
    
    [self checkPermissionCamera];
    [self setupSourceButtons];
}

- (void) setupSourceButtons {
    UIView* viewSourceGallery = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 62, 62)];
    
    buttonSourceGallery = [[UIImageView alloc] initWithImage:[UIImage imageNamed:self.delegate.namedImageForButtonSourceGallery]];
    buttonSourceGallery.contentMode = UIViewContentModeScaleAspectFit;
    buttonSourceGallery.frame = CGRectMake(9, 9, 44, 44);
    [viewSourceGallery addSubview:buttonSourceGallery];
    viewSourceGallery.tag = TAG_SOURCE_GALLERY_VIEW;
    
    [itemsSources addObject:viewSourceGallery];
    
    //------------------------------
    UIView* viewSourcePhoto = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 62, 62)];
    viewSourcePhoto.tag = TAG_SOURCE_PHOTO_VIEW;
    
    buttonSourcePhoto = [[UIImageView alloc] initWithImage:[UIImage imageNamed:self.delegate.namedImageForButtonSourcePhoto]];
    buttonSourcePhoto.frame = CGRectMake(9, 9, 44, 44);
    buttonSourcePhoto.contentMode = UIViewContentModeScaleAspectFit;
    buttonSourcePhotoActive.alpha = 0;
    [viewSourcePhoto addSubview:buttonSourcePhoto];
    
    buttonSourcePhotoActive = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 62, 62)];
    [buttonSourcePhotoActive setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonSourcePhotoActive] forState:UIControlStateNormal];
    [buttonSourcePhotoActive setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonSourcePhotoActiveHighlighted] forState:UIControlStateHighlighted];
    buttonSourcePhotoActive.contentMode = UIViewContentModeScaleAspectFit;
    [buttonSourcePhotoActive setTag:TAG_SOURCE_PHOTO_ACTIVE];
    [buttonSourcePhotoActive addTarget:self
               action:@selector(receivedTap:)
     forControlEvents:UIControlEventTouchUpInside];
    [viewSourcePhoto addSubview:buttonSourcePhotoActive];
    
    [itemsSources addObject:viewSourcePhoto];
    
    //------------------------------
    UIView* viewSourceVideo = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 62, 62)];
    viewSourceVideo.tag = TAG_SOURCE_VIDEO_VIEW;
    
    buttonSourceVideo = [[UIImageView alloc] initWithImage:[UIImage imageNamed:self.delegate.namedImageForButtonSourceVideo]];
    buttonSourceVideo.frame = CGRectMake(9, 9, 44, 44);
    buttonSourceVideo.contentMode = UIViewContentModeScaleAspectFit;
    [viewSourceVideo addSubview:buttonSourceVideo];
    
    buttonSourceVideoActive = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 62, 62)]; //0-0 to 60-60
    [buttonSourceVideoActive setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonSourceVideoActive] forState:UIControlStateNormal];
    [buttonSourceVideoActive setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonSourceVideoActiveHighlighted] forState:UIControlStateHighlighted];
    buttonSourceVideoActive.contentMode = UIViewContentModeScaleAspectFit;
    [buttonSourceVideoActive setTag:TAG_SOURCE_VIDEO_ACTIVE];
    buttonSourceVideoActive.alpha = 0;

    UILongPressGestureRecognizer *longPressGesture = [[UILongPressGestureRecognizer alloc] init];
    [longPressGesture addTarget:self action:@selector(buttonVideoLongPress:)];
    [buttonSourceVideoActive addGestureRecognizer:longPressGesture];

    [viewSourceVideo addSubview:buttonSourceVideoActive];
    
    [itemsSources addObject:viewSourceVideo];
    
}

- (void) viewDidAppear:(BOOL)animated {
    screenSizedRect = [[[self view] layer] bounds];
    previewLayer = [[UIView alloc] initWithFrame:screenSizedRect];
    [[self view] addSubview:previewLayer];
    previewLayer.backgroundColor = [UIColor blackColor];
    previewLayer.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    
    [self preview];
    [self buttons];
//    [self buttons];
/*    if ([AVCaptureDevice respondsToSelector:@selector(requestAccessForMediaType: completionHandler:)]) {
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
            // Will get here on both iOS 7 & 8 even though camera permissions weren't required
            // until iOS 8. So for iOS 7 permission will always be granted.
            if (!granted) {
                NSLog(@"Exit");
            }
        }];
        
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeAudio completionHandler:^(BOOL granted) {
            // Will get here on both iOS 7 & 8 even though camera permissions weren't required
            // until iOS 8. So for iOS 7 permission will always be granted.
            if (!granted) {
                NSLog(@"Exit");
            }
        }];
    }*/
}

- (void) checkPermissionCamera {
    if ([AVCaptureDevice respondsToSelector:@selector(requestAccessForMediaType: completionHandler:)]) {
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
            // Will get here on both iOS 7 & 8 even though camera permissions weren't required
            // until iOS 8. So for iOS 7 permission will always be granted.
            if (!granted) {
                [self.delegate cancelled];
                [self removeFromParentViewController];
            }
        }];
    }
}

- (void) checkPermissionLibrary {
    ALAuthorizationStatus status = [ALAssetsLibrary authorizationStatus];
    if (status != ALAuthorizationStatusAuthorized) {
        NSLog(@"Exit");
    }
}

- (void) receivedTap:(UITapGestureRecognizer *)gestureRecognizer{
    NSLog(@"receivedTap");
    [self takePicture];
}

- (void) buttonVideoLongPress:(UILongPressGestureRecognizer *)gestureRecognizer{
    if ([gestureRecognizer state] == UIGestureRecognizerStateBegan) {
        [buttonSourceVideoActive
         setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonSourceVideoActiveHighlighted]
         forState:UIControlStateNormal];
        [self startRecording];
    } else if([gestureRecognizer state] == UIGestureRecognizerStateEnded ||
              [gestureRecognizer state] == UIGestureRecognizerStateCancelled)  {
        [buttonSourceVideoActive
         setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonSourceVideoActive]
         forState:UIControlStateNormal];
        [self stopRecording];
    }
}

#pragma mark Interface Buttons

- (void) buttons {
    previewLayer = [[UIView alloc] initWithFrame:screenSizedRect];
    [self.view addSubview:previewLayer];
    
    CGRect headerRect = CGRectMake(0, 0, screenSizedRect.size.width, 60);
    headerBar = [[UIView alloc] initWithFrame:headerRect];
    headerBar.backgroundColor = [UIColor colorWithRed:255 green:255 blue:255 alpha:0.3];
    [self.view addSubview:headerBar];
    
    CGRect bodyRect = CGRectMake(0, 60, screenSizedRect.size.width, screenSizedRect.size.height - 160);
    bodyBar = [[UIView alloc] initWithFrame:bodyRect];
    [self.view addSubview:bodyBar];
    
    CGRect footerRect = CGRectMake(0, screenSizedRect.size.height - 100, screenSizedRect.size.width, 100);
    footerBar = [[UIView alloc] initWithFrame:footerRect];
    footerBar.backgroundColor = [UIColor colorWithRed:255 green:255 blue:255 alpha:0.3];
    [self.view addSubview:footerBar];
    
    //------------------------------------ Header
    
    buttonBack = [[UIButton alloc] initWithFrame:CGRectMake(0, 8, 44, 44)]; //0-0 to 60-60
    [buttonBack setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonBackImage] forState:UIControlStateNormal];
    [buttonBack setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonBackImageHighlighted] forState:UIControlStateHighlighted];
    buttonBack.contentMode = UIViewContentModeScaleAspectFit;
    [headerBar addSubview:buttonBack];
    
    buttonNext = [[UIButton alloc] initWithFrame:CGRectMake(screenSizedRect.size.width - 44, 8, 44, 44)]; //0-0 to 60-60
    [buttonNext setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonNextImage] forState:UIControlStateNormal];
    [buttonNext setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonNextImageHighlighted] forState:UIControlStateHighlighted];
    buttonNext.contentMode = UIViewContentModeScaleAspectFit;
    [headerBar addSubview:buttonNext];
    
    carouselPhotos = [[iCarousel alloc] initWithFrame:CGRectMake(60, 5, screenSizedRect.size.width - 120, 50)];
    [carouselPhotos setTag:TAG_CAROUSEL_PHOTOS];
    carouselPhotos.delegate = self;
    carouselPhotos.dataSource = self;
    [headerBar addSubview:carouselPhotos];
    
    //------------------------------------ Body
    
    if([UIImagePickerController isFlashAvailableForCameraDevice:UIImagePickerControllerCameraDeviceRear] || [UIImagePickerController isFlashAvailableForCameraDevice:UIImagePickerControllerCameraDeviceFront]) {
    
        buttonFlashOn = [[UIButton alloc] initWithFrame:CGRectMake(6, 20, 44, 44)]; //0-0 to 60-60
        [buttonFlashOn setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonFlashOnImage] forState:UIControlStateNormal];
        [buttonFlashOn setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonFlashOnImageHighlighted] forState:UIControlStateHighlighted];
        [buttonFlashOn addTarget:self action:@selector(switchFlash) forControlEvents:UIControlEventTouchUpInside];
        buttonFlashOn.contentMode = UIViewContentModeScaleAspectFit;
        buttonFlashOff.hidden = YES;
        [bodyBar addSubview:buttonFlashOn];
        
        buttonFlashOff = [[UIButton alloc] initWithFrame:CGRectMake(6, 20, 44, 44)]; //0-0 to 60-60
        [buttonFlashOff setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonFlashOff] forState:UIControlStateNormal];
        [buttonFlashOff setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonFlashOffHighlighted] forState:UIControlStateHighlighted];
        [buttonFlashOff addTarget:self action:@selector(switchFlash) forControlEvents:UIControlEventTouchUpInside];
        buttonFlashOff.contentMode = UIViewContentModeScaleAspectFit;
        buttonFlashOff.hidden = NO;
        [bodyBar addSubview:buttonFlashOff];
    }
    
    if([UIImagePickerController isCameraDeviceAvailable: UIImagePickerControllerCameraDeviceFront] && [UIImagePickerController isCameraDeviceAvailable: UIImagePickerControllerCameraDeviceRear]) {
        buttonSwitchCamera = [[UIButton alloc] initWithFrame:CGRectMake(screenSizedRect.size.width - 50, 20, 44, 44)];
        [buttonSwitchCamera setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonSwitchCamera] forState:UIControlStateNormal];
        [buttonSwitchCamera setBackgroundImage:[UIImage imageNamed:self.delegate.namedImageForButtonSwitchCameraHighlighted] forState:UIControlStateHighlighted];
        [buttonSwitchCamera addTarget:self action:@selector(switchCamera) forControlEvents:UIControlEventTouchUpInside];
        buttonSwitchCamera.contentMode = UIViewContentModeScaleAspectFit;
        [bodyBar addSubview:buttonSwitchCamera];
    }
    
    //------------------------------------ Footer
    
    carouselSources = [[iCarousel alloc] initWithFrame:CGRectMake(0, 20, screenSizedRect.size.width, 80)];
    [carouselSources setTag:TAG_CAROUSEL_SOURCES];
    carouselSources.delegate = self;
    carouselSources.dataSource = self;
    carouselSources.pagingEnabled = YES;
    [carouselSources setCurrentItemIndex:1];
    [footerBar addSubview:carouselSources];
    
    //--- Progress
    
    progressGray = [[UIView alloc] initWithFrame:CGRectMake(60, screenSizedRect.size.height - 85, 200, 1)];
    progressGray.backgroundColor = [UIColor lightGrayColor];
    [self.view addSubview:progressGray];
    progressRed = [[UIView alloc] initWithFrame:CGRectMake(60, screenSizedRect.size.height - 85, 0, 1)];
    progressRed.backgroundColor = [UIColor redColor];
    [self.view addSubview:progressRed];
//    [self performSelector:@selector(pauseProgressAnimation) withObject:nil afterDelay:10.0];
//    [self performSelector:@selector(resumeProgressAnimation) withObject:nil afterDelay:20.0];
//    [UIView animateWithDuration:0.25 animations:^{buttonSourceVideoActive.alpha = 0.0;}];
}

- (void) startProgressAnimation {
    [UIView animateWithDuration:30 animations:^{
        progressRed.frame = CGRectMake(progressRed.frame.origin.x, progressRed.frame.origin.y, 200, 1);
    }];
}

-(void) pauseProgressAnimation {
    CFTimeInterval pausedTime = [progressRed.layer convertTime:CACurrentMediaTime() fromLayer:nil];
    progressRed.layer.speed = 0.0;
    progressRed.layer.timeOffset = pausedTime;
}

-(void) resumeProgressAnimation {
    CFTimeInterval pausedTime = [progressRed.layer timeOffset];
    progressRed.layer.speed = 1.0;
    progressRed.layer.timeOffset = 0.0;
    progressRed.layer.beginTime = 0.0;
    CFTimeInterval timeSincePause = [progressRed.layer convertTime:CACurrentMediaTime() fromLayer:nil] - pausedTime;
    progressRed.layer.beginTime = timeSincePause;
}

#pragma mark -

#pragma mark Switch Methods

- (void)switchCamera {
    isUsingFrontCamera = !isUsingFrontCamera;
    [self removePreview];
    [self previewWithFronCamera:isUsingFrontCamera];
}

- (void)switchFlash {
    if (buttonFlashOn.hidden == YES) {
        //Turn Off
        buttonFlashOn.hidden = NO;
        buttonFlashOff.hidden = YES;
    } else {
        //Turn On
        buttonFlashOn.hidden = YES;
        buttonFlashOff.hidden = NO;
    }
}

- (void) displayImagePicker {
    elcPicker = [[ELCImagePickerController alloc] initImagePicker];
    elcPicker.maximumImagesCount = 4; //Set the maximum number of images to select, defaults to 4
    elcPicker.returnsOriginalImage = NO; //Only return the fullScreenImage, not the fullResolutionImage
    elcPicker.returnsImage = YES; //Return UIimage if YES. If NO, only return asset location information
    elcPicker.onOrder = YES; //For multiple image selection, display and return selected order of images
    elcPicker.imagePickerDelegate = self;
    [self presentViewController:elcPicker animated:YES completion:nil];
}

#pragma mark -

#pragma mark Take Picture Methods

-(void) preview {
    [self previewWithFronCamera:isUsingFrontCamera];
}

-(void) previewPositions {
    if([[UIDevice currentDevice] orientation] == UIDeviceOrientationPortrait) {
//        buttonSwitchCamera.frame = CGRectMake(200, 200, 44, 44);
    } else {
        
    }
}

- (void) previewWithFronCamera:(BOOL)frontCamera {
    [self setCaptureManager:[[CaptureSessionManager alloc] init]];
    [[self captureManager] addVideoInputFrontCamera:frontCamera];
    [[self captureManager] addStillImageOutput];
    [[self captureManager] addMovieFileOutputWith:30 MaxDuration:maxVideoTime];
    [[self captureManager] addVideoPreviewLayer];
    CGRect layerRect = [previewLayer bounds];
    [[[self captureManager] previewLayer] setBounds:layerRect];
    [[[self captureManager] previewLayer] setPosition:CGPointMake(CGRectGetMidX(layerRect),
                                                                  CGRectGetMidY(layerRect))];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(imageCaptured) name:kImageCapturedSuccessfully object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(videoCaptured) name:kVideoCapturedSuccessfully object:nil];
    [[previewLayer layer] addSublayer:[[self captureManager] previewLayer]];
    [[captureManager captureSession] startRunning];
    
}

- (void) removePreview {
    [[[self captureManager] previewLayer] removeFromSuperlayer];
    [[self captureManager] stopRunning];
    [self setCaptureManager:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:kImageCapturedSuccessfully object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:kVideoCapturedSuccessfully object:nil];
}

- (void) takePicture {
    [[self captureManager] captureStillImage];
}

- (void)imageCaptured {
    if([self saveToDevice]) {
        UIImageWriteToSavedPhotosAlbum([[self captureManager] stillImage], self, @selector(image:didFinishSavingWithError:contextInfo:), nil);
    } else {
        [self addImageToTakenCarousel];
    }
}
- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)contextInfo
{
    if (error != NULL) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Error!" message:@"Image couldn't be saved" delegate:self cancelButtonTitle:@"Ok" otherButtonTitles:nil, nil];
        [alert show];
    } else {
        [self addImageToTakenCarousel];
    }
}
- (void)addImageToTakenCarousel {
    NSInteger index = MAX(0, carouselPhotos.currentItemIndex);
    [itemsPhotos insertObject:[[self captureManager] stillImage] atIndex:(NSUInteger)index];
    [carouselPhotos insertItemAtIndex:index animated:YES];
}

- (void) startRecording {
    if(itemMovie == nil)
        [self startProgressAnimation];
    else
        [self resumeProgressAnimation];
    [[self captureManager] startRecording];
}

- (void) stopRecording {
    [[self captureManager] stopRecording];
    [self pauseProgressAnimation];
}

- (void)videoCaptured
{
    NSLog(@"videoCaptured");
    UISaveVideoAtPathToSavedPhotosAlbum([[[self captureManager] movieFile] relativePath], self, @selector(video:didFinishSavingWithError:contextInfo:), nil);
    
    NSLog(@"videoCaptured: %@", [[[self captureManager] movieFile] relativePath]);
}

- (void) video: (NSString *) newItemMovie didFinishSavingWithError: (NSError *) error contextInfo: (void *) contextInfo {
    NSLog(@"didFinishSavingWithError");
    if(shouldMergeVideos && itemMovie != nil) {
        AVAsset* firstAsset = [AVAsset assetWithURL:[NSURL fileURLWithPath:itemMovie]];
        NSLog(@"firstAsset.duration %f", CMTimeGetSeconds(firstAsset.duration));
        AVAsset* secondAsset = [AVAsset assetWithURL:[NSURL fileURLWithPath:newItemMovie]];
        NSLog(@"secondAsset.duration %f", CMTimeGetSeconds(secondAsset.duration));
        AVMutableComposition *mixComposition = [[AVMutableComposition alloc] init];
        AVMutableCompositionTrack *firstTrack = [mixComposition addMutableTrackWithMediaType:AVMediaTypeVideo
                                                                            preferredTrackID:kCMPersistentTrackID_Invalid];
        
        [firstTrack insertTimeRange:CMTimeRangeMake(kCMTimeZero, firstAsset.duration)
                            ofTrack:[[firstAsset tracksWithMediaType:AVMediaTypeVideo] objectAtIndex:0] atTime:kCMTimeZero error:nil];
        [firstTrack insertTimeRange:CMTimeRangeMake(kCMTimeZero, secondAsset.duration)
                            ofTrack:[[secondAsset tracksWithMediaType:AVMediaTypeVideo] objectAtIndex:0] atTime:firstAsset.duration error:nil];

        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *documentsDirectory = [paths objectAtIndex:0];
        NSString *myPathDocs =  [documentsDirectory stringByAppendingPathComponent:
                                 [NSString stringWithFormat:@"mergeVideo-%d.mov",arc4random() % 1000]];
        NSURL *url = [NSURL fileURLWithPath:myPathDocs];
        
        // 5 - Create exporter
        AVAssetExportSession *exporter = [[AVAssetExportSession alloc] initWithAsset:mixComposition
                                                                          presetName:AVAssetExportPresetHighestQuality];
        exporter.outputURL=url;
        exporter.outputFileType = AVFileTypeQuickTimeMovie;
        exporter.shouldOptimizeForNetworkUse = YES;
        [exporter exportAsynchronouslyWithCompletionHandler:^{
            dispatch_async(dispatch_get_main_queue(), ^{
                [self exportDidFinish:exporter];
            });
        }];
    } else {
        NSLog(@"itemMovie = newItemMovie");
        itemMovie = newItemMovie;
    }
}
-(void)exportDidFinish:(AVAssetExportSession*)session {
    NSLog(@"exportDidFinish");
    if (session.status == AVAssetExportSessionStatusCompleted) {
        NSURL *outputURL = session.outputURL;
        ALAssetsLibrary *library = [[ALAssetsLibrary alloc] init];
        if ([library videoAtPathIsCompatibleWithSavedPhotosAlbum:outputURL]) {
            [library writeVideoAtPathToSavedPhotosAlbum:outputURL completionBlock:^(NSURL *assetURL, NSError *error){
                dispatch_async(dispatch_get_main_queue(), ^{
                    if (error) {
                        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Error" message:@"Video Saving Failed"
                                                                       delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                        [alert show];
                    } else {
                        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Video Saved" message:@"Saved To Photo Album"
                                                                       delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
                        [alert show];
                    }
                });
            }];
        }
    }
/*    audioAsset = nil;
    firstAsset = nil;
    secondAsset = nil;
    [activityView stopAnimating];*/
}

#pragma mark -
#pragma mark iCarousel methods

- (NSInteger)numberOfItemsInCarousel:(iCarousel *)carousel
{
    if(carousel.tag == TAG_CAROUSEL_PHOTOS) {
        return itemsPhotos.count;
    } else if(carousel.tag == TAG_CAROUSEL_SOURCES) {
        return itemsSources.count;
    }
    
    return 0;
}

- (UIView *)carousel:(iCarousel *)carousel viewForItemAtIndex:(NSInteger)index reusingView:(UIView *)view
{
    if(carousel.tag == TAG_CAROUSEL_PHOTOS) {
        if (view == nil)
        {
            UIImageView *imageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 40, 50)];
            imageView.contentMode = UIViewContentModeScaleAspectFit;
            view = imageView;
        }
        ((UIImageView *)view).image = itemsPhotos[index];
    } else if(carousel.tag == TAG_CAROUSEL_SOURCES) {
        if (view == nil)
        {
            view = [itemsSources objectAtIndex:index];
        }
    }
    
    return view;
}

- (CGFloat)carousel:(iCarousel *)carousel valueForOption:(iCarouselOption)option withDefault:(CGFloat)value
{
    if(carousel.tag == TAG_CAROUSEL_PHOTOS) {
        
        switch (option)
        {
            case iCarouselOptionFadeMin:
                return -0.2;
            case iCarouselOptionFadeMax:
                return 0.2;
            case iCarouselOptionFadeRange:
                return 2.0;
            case iCarouselOptionWrap:
                return 1;
            case iCarouselOptionVisibleItems:
                return 5;
            case iCarouselOptionSpacing:
                return 1;
            default:
                return value;
        }
    } else if(carousel.tag == TAG_CAROUSEL_SOURCES) {
        switch (option)
        {
            case iCarouselOptionFadeMin:
                return -0.2;
            case iCarouselOptionFadeMax:
                return 0.2;
            case iCarouselOptionFadeRange:
                return 2.0;
            case iCarouselOptionWrap:
                return 0;
            case iCarouselOptionVisibleItems:
                return 3;
            case iCarouselOptionSpacing:
                return 1.5;
            default:
                return value;
        }
    }
    return value;
}

- (CGFloat)carouselItemWidth:(iCarousel *)carousel {
    if(carousel.tag == TAG_CAROUSEL_SOURCES) {
        return 62;
    } else if(carousel.tag == TAG_CAROUSEL_PHOTOS) {
        return 40;
    }
    return 0;
}

- (void) carousel:(iCarousel *)carousel didSelectItemAtIndex:(NSInteger)index {
    if(carousel.tag == TAG_CAROUSEL_PHOTOS) {
        UIAlertController* confirmation = [UIAlertController alertControllerWithTitle:@"Move" message:textDeletePhoto preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction* dismiss = [UIAlertAction actionWithTitle:@"Dismiss" style:UIAlertActionStyleCancel handler:^(UIAlertAction * action)
                                  {
                                      [confirmation dismissViewControllerAnimated:YES completion:nil];
                                  }];
        [confirmation addAction:dismiss];
        UIAlertAction* delete = [UIAlertAction actionWithTitle:@"Delete" style:UIAlertActionStyleDestructive handler:^(UIAlertAction * action)
                                 {
                                     if([carouselPhotos itemViewAtIndex:index] != nil) {
                                         [itemsPhotos removeObjectAtIndex:(NSUInteger)index];
                                         [carouselPhotos removeItemAtIndex:index animated:YES];
                                     }
                                     [confirmation dismissViewControllerAnimated:YES completion:nil];
                                 }];
        [confirmation addAction:delete];
        [self presentViewController:confirmation animated:YES completion:nil];
/*    } else if (carousel.tag == TAG_CAROUSEL_SOURCES) {
        if(carousel.currentItemView.tag == TAG_SOURCE_GALLERY_VIEW) {
            [self displayImagePicker];
        }*/
    } else if(carousel.tag == TAG_CAROUSEL_SOURCES) {
        UIView* currentItemView = [itemsSources objectAtIndex:index];
        if (currentItemView.tag == TAG_SOURCE_PHOTO_VIEW) {
            //            buttonSourcePhoto.hidden = YES;
            [UIView animateWithDuration:0.1 animations:^{buttonSourcePhoto.alpha = 0.0;}];
            //            buttonSourcePhotoActive.hidden = NO;
            [UIView animateWithDuration:0.1 animations:^{buttonSourcePhotoActive.alpha = 1.0;}];
        } else if (currentItemView.tag == TAG_SOURCE_VIDEO_VIEW) {
            //            buttonSourceVideo.hidden = YES;
            [UIView animateWithDuration:0.1 animations:^{buttonSourceVideo.alpha = 0.0;}];
            //            buttonSourceVideoActive.hidden = NO;
            [UIView animateWithDuration:0.1 animations:^{buttonSourceVideoActive.alpha = 1.0;}];
        } else if(currentItemView.tag == TAG_SOURCE_GALLERY_VIEW) {
            [self displayImagePicker];
        }
    }
}

- (void) carouselWillBeginScrollingAnimation:(iCarousel *)carousel {
    if(carousel.tag == TAG_CAROUSEL_SOURCES) {
        [UIView animateWithDuration:0.25 animations:^{buttonSourcePhoto.alpha = 1.0;}];
        [UIView animateWithDuration:0.25 animations:^{buttonSourcePhotoActive.alpha = 0.0;}];
        [UIView animateWithDuration:0.25 animations:^{buttonSourceVideo.alpha = 1.0;}];
        [UIView animateWithDuration:0.25 animations:^{buttonSourceVideoActive.alpha = 0.0;}];
    }
}

- (void) carouselDidEndScrollingAnimation:(iCarousel *)carousel {
    UIView* currentItemView = carousel.currentItemView;
    if (currentItemView.tag == TAG_SOURCE_PHOTO_VIEW) {
        buttonSourcePhoto.alpha = 0;
        buttonSourcePhotoActive.alpha = 1;
    } else if (currentItemView.tag == TAG_SOURCE_VIDEO_VIEW) {
        buttonSourceVideo.alpha = 0;
        buttonSourceVideoActive.alpha = 1;
    }
}

#pragma mark -

- (void) elcImagePickerController:(ELCImagePickerController *)picker didFinishPickingMediaWithInfo:(NSArray *)info {
    [picker dismissViewControllerAnimated:YES completion:nil];
    [carouselSources setCurrentItemIndex:1];
}

- (void)elcImagePickerControllerDidCancel:(ELCImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:nil];
    [carouselSources setCurrentItemIndex:1];
}


/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (BOOL)prefersStatusBarHidden { return YES; }
- (BOOL)shouldAutorotate { return false; }
- (void)didReceiveMemoryWarning { [super didReceiveMemoryWarning]; }

@end
