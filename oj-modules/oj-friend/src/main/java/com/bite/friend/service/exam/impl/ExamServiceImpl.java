package com.bite.friend.service.exam.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.common.core.util.ThreadLocalUtil;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.domain.exam.vo.ExamVO;
import com.bite.friend.manger.ExamCacheManager;
import com.bite.friend.mapper.exam.ExamMapper;
import com.bite.friend.service.exam.IExamService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamServiceImpl implements IExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamCacheManager examCacheManager;

    /*
     * 查询竞赛列表（老接口--> 未加入redis优化）
     */
    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(),examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    /*
     * 查询竞赛列表（新接口--> 加入了redis优化）
     */
    @Override
    public TableDataInfo redisList(ExamQueryDTO examQueryDTO) {
        // 如果有时间过滤条件，直接查数据库
        if (examQueryDTO.getStartTime() != null || examQueryDTO.getEndTime() != null) {
            List<ExamVO> examVOList = list(examQueryDTO);
            long total = new PageInfo<>(examVOList).getTotal();
            return TableDataInfo.success(examVOList, total);
        }

        // 没有时间过滤，走缓存逻辑
        //从redis中获取 竞赛列表数据
        Long total = examCacheManager.getListSize(examQueryDTO.getType(), null);
        List<ExamVO> examVOList;
        if (total == null || total <= 0){
            //从数据库中获取 竞赛列表数据
            examVOList = list(examQueryDTO);
            //同步到redis中
            examCacheManager.refreshCache(examQueryDTO.getType(), null);
            total = new PageInfo<>(examVOList).getTotal(); //获取总记录数
        } else {
            //从redis中获取 竞赛列表数据
            examVOList = examCacheManager.getExamVOList(examQueryDTO, null);
            total =  examCacheManager.getListSize(examQueryDTO.getType(), null); // 获取总记录数
        }
        if (CollectionUtil.isEmpty(examVOList)){ //使用hutool工具包判断集合是否为空
            return TableDataInfo.empty(); //未查出任何数据时调用
        }
        assembleExamVOList(examVOList); //判断当前用户是否参加竞赛
        return TableDataInfo.success(examVOList, total);
    }

    /*
     * 获取竞赛的第一道题
     */
    @Override
    public String getFirstQuestion(Long examId) {

        //先判断缓存中有没有数据
        Long listSize = examCacheManager.getExamQuestionListSize(examId);
        if (listSize == null || listSize <= 0){
            examCacheManager.refreshExamQuestionCache(examId); //现在刷新的是竞赛中题目列表数据
        }

        return examCacheManager.getFirstQuestion(examId).toString();
    }

    /*
     * 获取上一题（竞赛内）
     */
    @Override
    public String preQuestion(Long examId, Long questionId) {
        checkAndRefresh(examId);
        //到这里才去redis中获取上一题的 id
        return examCacheManager.preQuestion(examId, questionId).toString();
    }


    /*
     * 获取下一题(竞赛内)
     */
    @Override
    public String nextQuestion(Long examId, Long questionId) {
        checkAndRefresh(examId);
        //到这里才去redis中获取上一题的 id
        return examCacheManager.nextQuestion(examId, questionId).toString();
    }


    private void checkAndRefresh(Long examId) { //检测缓存数据
        Long listSize = examCacheManager.getExamQuestionListSize(examId);
        if (listSize == null || listSize == 0){ // Redis中没有数据
            examCacheManager.refreshExamQuestionCache(examId); // 同步数据
        }
    }


    /*
     * 判断当前用户是否参加竞赛
     */
    private void assembleExamVOList(List<ExamVO> examVOList) {
        //先拿到当前用户id
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        //获取当前用户所有已报名的竞赛列表
        List<Long> userExamIdList = examCacheManager.getAllUserExamList(userId);
        if (CollectionUtil.isEmpty(userExamIdList)){
            return;
        }
        for (ExamVO examVO : examVOList) { //遍历所有竞赛列表数据(查看竞赛是否包含在用户我的竞赛里)
            if (userExamIdList.contains(examVO.getExamId())) {
                examVO.setEnter(true);
            }
        }
    }


}
