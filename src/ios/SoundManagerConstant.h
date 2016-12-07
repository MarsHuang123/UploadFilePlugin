//
//  SoundManagerConstant.h
//  AFSoundManager-Demo
//
//  Created by mars on 16/7/7.
//  Copyright © 2016年 AlvaroFranco. All rights reserved.
//

#ifndef SoundManagerConstant_h
#define SoundManagerConstant_h

#define kBlockSize 20

#define kApiKey @"a2ff41f0-ded0-4040-ab17-ba0f1aa4e12b"

#define Data @"DataForder"

#define CachesPath(name) [[[NSHomeDirectory() stringByAppendingPathComponent:@"Library"] stringByAppendingPathComponent:@"Caches"] stringByAppendingPathComponent:name]
#define StructurePath(name) [[[[NSHomeDirectory() stringByAppendingPathComponent:@"Library"] stringByAppendingPathComponent:@"Caches"] stringByAppendingPathComponent:@"StructureFolder"] stringByAppendingPathComponent:name]
#define DataPath(name) [[[[NSHomeDirectory() stringByAppendingPathComponent:@"Library"] stringByAppendingPathComponent:@"Caches"] stringByAppendingPathComponent:Data] stringByAppendingPathComponent:name]

#define CheckNilAndNull(obj) ((obj == nil || [obj isKindOfClass:[NSNull class]]) ? YES : NO)
#define kBaseURL @"http://csdtest.acer.com.cn/MobilityCssApi/api/CaseList"
#define kUploadVoiceFile  @"/UploadVoiceFile"
#define kGetUploadVoiceFileProgress  @"/GetUploadVoiceFileProgress"


#endif /* SoundManagerConstant_h */