package network.service;

import io.tarantool.driver.api.TarantoolClient;
import network.dto.DialogueMessage;
import network.dto.NewMessageModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class DialogueService {
    private final TarantoolClient tarantoolClient;

    public DialogueService(TarantoolClient tarantoolClient) {
        this.tarantoolClient = tarantoolClient;
    }


    public List<DialogueMessage> getAllMessages(String userId, String friendId) {
        try {
            var obj = (List<Object>) tarantoolClient.call("get_data", generateDialogId(userId, friendId)).get().get(0);
            return obj.stream()
                    .map(el -> {
                        var arr = (List<Object>) el;
                        return  new DialogueMessage(arr.get(2).toString(), arr.get(3).toString(), arr.get(4).toString());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public void createMessage(String userId, String friendId, NewMessageModel body) {
        tarantoolClient.call("load_data", generateDialogId(userId, friendId), userId, friendId, body.getText());
    }

    private String generateDialogId(String userId, String friendId) {
        var dialogId = StringUtils.compare(userId, friendId) < 0 ? userId + friendId : friendId + userId;
        return UUID.nameUUIDFromBytes(dialogId.getBytes()).toString();
    }
}
