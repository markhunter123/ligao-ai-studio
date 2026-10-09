package ligao.planning.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.planning.model.PlanEntity;

/**
 * 计划 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface PlanMapper extends BaseMapper<PlanEntity> {
}
