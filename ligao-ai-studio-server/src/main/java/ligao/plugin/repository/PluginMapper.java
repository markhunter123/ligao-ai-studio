package ligao.plugin.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.plugin.model.PluginEntity;

/**
 * MyBatis Plus mapper for ligao_plugin table.
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface PluginMapper extends BaseMapper<PluginEntity> {
}
