package ligao.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.tool.model.ToolEntity;

/**
 * 工具 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface ToolMapper extends BaseMapper<ToolEntity> {
}
