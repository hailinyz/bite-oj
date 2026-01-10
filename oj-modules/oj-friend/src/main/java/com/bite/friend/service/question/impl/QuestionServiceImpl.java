package com.bite.friend.service.question.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.question.Question;
import com.bite.friend.domain.question.dto.QuestionQueryDTO;
import com.bite.friend.domain.question.es.QuestionES;
import com.bite.friend.domain.question.vo.QuestionDetailVO;
import com.bite.friend.domain.question.vo.QuestionVO;
import com.bite.friend.elasticsearch.QuestionRepository;
import com.bite.friend.mapper.question.QuestionMapper;
import com.bite.friend.service.question.IQuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements IQuestionService {


    @Autowired
    private QuestionRepository questionRespository;

    @Autowired
    private QuestionMapper questionMapper;


    /*
    查询题目列表
     */
    @Override
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO) {

        long count = questionRespository.count(); //先会让它试探性的判断ES中是否有数据
        if (count <= 0){
            refreshQuestion(); //刷新ES,同步数据
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(questionQueryDTO.getPageNum() - 1, questionQueryDTO.getPageSize(), sort); // 分页参数
        Integer difficulty = questionQueryDTO.getDifficulty();
        String keyword = questionQueryDTO.getKeyword();

        //返回的形式
        Page<QuestionES> questionESPage;

        if (difficulty == null && StrUtil.isEmpty(keyword)){
            questionESPage = questionRespository.findAll(pageable);
        } else if (StrUtil.isEmpty(keyword)) {
            questionESPage = questionRespository.findQuestionByDifficulty(difficulty, pageable);
        } else if (difficulty == null) {
            questionESPage = questionRespository.findByTitleOrContent(keyword, keyword, pageable);
        } else {
            questionESPage = questionRespository.findByTitleOrContentAndDifficulty(keyword, keyword, difficulty, pageable);
        }

        long total = questionESPage.getTotalElements();
        if (total <= 0){
            return TableDataInfo.empty();
        }
        List<QuestionES> questionESList = questionESPage.getContent();
        // 将QuestionES转换成QuestionVO，因为在QuestionES中前端不需要的字段太多了，所以这里只转换一部分字段
        List<QuestionVO> questionVOList = BeanUtil.copyToList(questionESList, QuestionVO.class);
        return TableDataInfo.success(questionVOList, total);

    }

    /*
    获取题目详情
     */
    @Override
    public QuestionDetailVO detail(Long questionId) {

        // 从ES中查询
        QuestionES questionES = questionRespository.findById(questionId).orElse(null); //获取的是options字段,所以需要.orElse(null)
        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        if (questionES != null){
            BeanUtils.copyProperties(questionES,questionDetailVO);
            return questionDetailVO;
        }

        // 从数据库中查询
        Question question = questionMapper.selectById(questionId);
        if (question == null){
            return null;
        }
        refreshQuestion(); //将数据库中的数据同步到ES中
        BeanUtils.copyProperties(question,questionDetailVO);
        return questionDetailVO;
    }

    /*
    刷新题目从数据库中查
     */
    private void refreshQuestion() {
        //查询数据库的题目
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>());
        if (CollectionUtil.isEmpty(questionList)){
            return;
        }

        List<QuestionES> questionESList = BeanUtil.copyToList(questionList, QuestionES.class);
        questionRespository.saveAll(questionESList);
    }


}
