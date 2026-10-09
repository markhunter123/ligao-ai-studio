package ligao.workspace.conversation.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.workspace.conversation.model.MessageEntity;

/**
 * 消息数据访问层
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface MessageMapper extends BaseMapper<MessageEntity> {
}
