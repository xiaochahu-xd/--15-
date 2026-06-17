package com.coursework.system.assignment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursework.system.assignment.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
