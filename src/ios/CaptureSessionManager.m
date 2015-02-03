
#import "CaptureSessionManager.h"
#import <ImageIO/ImageIO.h>

@implementation CaptureSessionManager

@synthesize captureSession;
@synthesize previewLayer;
@synthesize stillImageOutput;
@synthesize stillImage;
@synthesize movieFileOutput;

#pragma mark Capture Session Configuration

bool isRecording = NO;

- (id)init {
    if ((self = [super init])) {
        [self setCaptureSession:[[AVCaptureSession alloc] init]];
    }
    return self;
}

- (void)addVideoPreviewLayer {
    [self setPreviewLayer:[[AVCaptureVideoPreviewLayer alloc] initWithSession:[self captureSession]]];
    [[self previewLayer] setVideoGravity:AVLayerVideoGravityResizeAspectFill];
    
}

- (void)addVideoInputFrontCamera:(BOOL)front {
    NSArray *devices = [AVCaptureDevice devices];
    AVCaptureDevice *frontCamera;
    AVCaptureDevice *backCamera;
    
    for (AVCaptureDevice *device in devices) {
        
        NSLog(@"Device name: %@", [device localizedName]);
        
        if ([device hasMediaType:AVMediaTypeVideo]) {
            
            if ([device position] == AVCaptureDevicePositionBack) {
                NSLog(@"Device position : back");
                backCamera = device;
            }
            else {
                NSLog(@"Device position : front");
                frontCamera = device;
            }
        }
    }
    
    NSError *error = nil;
    
    
    
    if (front) {
        AVCaptureDeviceInput *frontFacingCameraDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:frontCamera error:&error];
        if (!error) {
            if ([[self captureSession] canAddInput:frontFacingCameraDeviceInput]) {
                //[self removeInput];
                [[self captureSession] addInput:frontFacingCameraDeviceInput];
                NSLog(@"Added front facing video input");
            } else {
                NSLog(@"Couldn't add front facing video input");
            }
        }
    } else {
        AVCaptureDeviceInput *backFacingCameraDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:backCamera error:&error];
        if (!error) {
            if ([[self captureSession] canAddInput:backFacingCameraDeviceInput]) {
                //[self removeInput];
                [[self captureSession] addInput:backFacingCameraDeviceInput];
                NSLog(@"Added back facing video input");
            } else {
                NSLog(@"Couldn't add back facing video input");
            }
        }
    }
    AVCaptureDevice *audioCaptureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeAudio];
    AVCaptureDeviceInput *audioInput = [AVCaptureDeviceInput deviceInputWithDevice:audioCaptureDevice error:&error];
    if (audioInput)
    {
        [captureSession addInput:audioInput];
    }
}

-   (void) removeInput {
    if([[[self captureSession] inputs] count] == 2)
        [[self captureSession] removeInput: [[[self captureSession] inputs] objectAtIndex:0]];
    if([[[self captureSession] inputs] count] == 1)
        [[self captureSession] removeInput: [[[self captureSession] inputs] objectAtIndex:0]];
}

- (void)addStillImageOutput
{
//    [[AVCaptureOutput]]
    [self setStillImageOutput:[[AVCaptureStillImageOutput alloc] init]];
    NSDictionary *outputSettings = [[NSDictionary alloc] initWithObjectsAndKeys:AVVideoCodecJPEG,AVVideoCodecKey,nil];
    [[self stillImageOutput] setOutputSettings:outputSettings];
    
    AVCaptureConnection *videoConnection = nil;
    for (AVCaptureConnection *connection in [[self stillImageOutput] connections]) {
        for (AVCaptureInputPort *port in [connection inputPorts]) {
            if ([[port mediaType] isEqual:AVMediaTypeVideo] ) {
                videoConnection = connection;
                break;
            }
        }
        if (videoConnection) {
            break;
        }
    }
    
    [[self captureSession] addOutput:[self stillImageOutput]];
}

- (void)captureStillImage
{
    AVCaptureConnection *videoConnection = nil;
    NSArray* connections = [[self stillImageOutput] connections];
    for (AVCaptureConnection *connection in connections) {
        for (AVCaptureInputPort *port in [connection inputPorts]) {
            if ([[port mediaType] isEqual:AVMediaTypeVideo]) {
                videoConnection = connection;
                break;
            }
        }
        if (videoConnection) {
            break;
        }
    }
    
    NSLog(@"about to request a capture from: %@", [self stillImageOutput]);
    [[self stillImageOutput] captureStillImageAsynchronouslyFromConnection:videoConnection
                                                         completionHandler:^(CMSampleBufferRef imageSampleBuffer, NSError *error) {

                                                             NSData* imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageSampleBuffer];
                                                             
                                                             UIImage *image = [[UIImage alloc] initWithData:imageData];
                                                             [self setStillImage:image];
                                                             
                                                             [[NSNotificationCenter defaultCenter] postNotificationName:kImageCapturedSuccessfully object:nil];
                                                         }];
}

- (void)stopRunning {
    [[self captureSession] stopRunning];
}

//Extras:
- (void)addMovieFileOutputWith:(Float64)fps MaxDuration:(int32_t)duration {
    movieFileOutput = [[AVCaptureMovieFileOutput alloc] init];
    movieFileOutput.maxRecordedDuration = CMTimeMakeWithSeconds(duration, fps);

    if ([captureSession canAddOutput:movieFileOutput]) {
        NSLog(@"captureSession canAddOutput:movieFileOutput");
        [captureSession addOutput:movieFileOutput];
    } else {
        NSLog(@"ERROR captureSession canAddOutput:movieFileOutput");
    }
    
    //SET THE CONNECTION PROPERTIES (output properties)
//    AVCaptureConnection *CaptureConnection = [movieFileOutput connectionWithMediaType:AVMediaTypeVideo];
    
    //Set landscape (if required)
/*    if ([CaptureConnection isVideoOrientationSupported])
    {
        AVCaptureVideoOrientation orientation = AVCaptureVideoOrientationLandscapeRight;		//<<<<<SET VIDEO ORIENTATION IF LANDSCAPE
        [CaptureConnection setVideoOrientation:orientation];
    }*/
    //----- SET THE IMAGE QUALITY / RESOLUTION -----
    //Options:
    //	AVCaptureSessionPresetHigh - Highest recording quality (varies per device)
    //	AVCaptureSessionPresetMedium - Suitable for WiFi sharing (actual values may change)
    //	AVCaptureSessionPresetLow - Suitable for 3G sharing (actual values may change)
    //	AVCaptureSessionPreset640x480 - 640x480 VGA (check its supported before setting it)
    //	AVCaptureSessionPreset1280x720 - 1280x720 720p HD (check its supported before setting it)
    //	AVCaptureSessionPresetPhoto - Full photo resolution (not supported for video output)
    NSLog(@"Setting image quality");

//    [captureSession setSessionPreset:AVCaptureSessionPresetMedium];
//    if ([captureSession canSetSessionPreset:AVCaptureSessionPreset640x480])
//        [captureSession setSessionPreset:AVCaptureSessionPreset640x480];
}

- (void)startRecording {
    if (!isRecording)
    {
        //----- START RECORDING -----
        NSLog(@"START RECORDING");
        isRecording = YES;
        
        //Create temporary URL to record to
        NSTimeInterval timeStamp = [[NSDate date] timeIntervalSince1970];
        NSString *outputPath = [[NSString alloc] initWithFormat:@"%@%@",
                                NSTemporaryDirectory(),
                                [NSString stringWithFormat:@"output-%f.mov", timeStamp]];
        NSLog(@"%@", outputPath);
        NSURL *outputURL = [[NSURL alloc] initFileURLWithPath:outputPath];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        if ([fileManager fileExistsAtPath:outputPath])
        {
            NSError *error;
            if ([fileManager removeItemAtPath:outputPath error:&error] == NO)
            {
                NSLog(@"ERROR RECORDING");
                //Error - handle if requried
            }
        }
        //Start recording
        [movieFileOutput startRecordingToOutputFileURL:outputURL recordingDelegate:self];
    }
}
- (void)captureOutput:(AVCaptureFileOutput *)captureOutput didStartRecordingToOutputFileAtURL:(NSURL *)fileURL fromConnections:(NSArray *)connections {
    NSLog(@"captureOutput");
    NSLog(@"captureOutput: %@", [fileURL relativePath]);
    
}
- (void)stopRecording {
    if (isRecording)
    {
        //----- STOP RECORDING -----
        NSLog(@"STOP RECORDING");
        isRecording = NO;
        
        [movieFileOutput stopRecording];
    }
}

- (void)captureOutput:(AVCaptureFileOutput *)captureOutput
didFinishRecordingToOutputFileAtURL:(NSURL *)outputFileURL
      fromConnections:(NSArray *)connections
                error:(NSError *)error
{
    
    NSLog(@"didFinishRecordingToOutputFileAtURL - enter: %@", [outputFileURL relativePath]);
    
    BOOL RecordedSuccessfully = YES;
    if ([error code] != noErr)
    {
        // A problem occurred: Find out if the recording was successful.
        id value = [[error userInfo] objectForKey:AVErrorRecordingSuccessfullyFinishedKey];
        if (value)
        {
            RecordedSuccessfully = [value boolValue];
        }
    }
    if (RecordedSuccessfully)
    {
        NSLog(@"didFinishRecordingToOutputFileAtURL - success");
        ALAssetsLibrary *library = [[ALAssetsLibrary alloc] init];
        if ([library videoAtPathIsCompatibleWithSavedPhotosAlbum:outputFileURL]) {
            [library writeVideoAtPathToSavedPhotosAlbum:outputFileURL completionBlock:^(NSURL *assetURL, NSError *error) {
                 if (error) {
                     
                 } else {
                     self.movieFile = outputFileURL;
                     [[NSNotificationCenter defaultCenter] postNotificationName:kVideoCapturedSuccessfully object:nil];
                 }
             }];
        }
    }
}

@end