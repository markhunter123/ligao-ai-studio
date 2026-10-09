package ligao.channel.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.channel.model.ChannelSessionEntity;

/**
 * 渠道会话 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface ChannelSessionMapper extends BaseMapper<ChannelSessionEntity> {
}
