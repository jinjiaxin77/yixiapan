package com.jinjiaxin.yixiapan.task;

import com.jinjiaxin.yixiapan.entity.enums.FileDelFlagEnums;
import com.jinjiaxin.yixiapan.entity.pojo.FileInfo;
import com.jinjiaxin.yixiapan.entity.query.FileInfoQuery;
import com.jinjiaxin.yixiapan.service.FileInfoService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FileCleanTask {
    @Resource
    private FileInfoService fileInfoService;

    @Scheduled(fixedDelay = 1000*60*30)
    public void execute(){
        FileInfoQuery query = new FileInfoQuery();
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        query.setQueryExpire(true);
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(query);
        Map<String,List<FileInfo>> fileInfoMap = fileInfoList.stream().collect(Collectors.groupingBy(FileInfo::getUserId));
        for(Map.Entry<String,List<FileInfo>> entry : fileInfoMap.entrySet()){
            List<String> fileIds = entry.getValue().stream().map(p->p.getFileId()).collect(Collectors.toList());
            fileInfoService.delFileBatch(entry.getKey(),String.join(",",fileIds),false);
        }
    }
}
