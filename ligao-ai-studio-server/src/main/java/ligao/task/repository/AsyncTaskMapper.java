package ligao.task.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.task.model.AsyncTaskEntity;

/**
 * 异步任务 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface AsyncTaskMapper extends BaseMapper<AsyncTaskEntity> {
}
