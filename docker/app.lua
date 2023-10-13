#!/usr/bin/env tarantool

local log = require('log')
local uuid = require('uuid')

local function init()
    local dialogue_space = box.schema.space.create('dialogue', {
        if_not_exists = true
    })

    dialogue_space:format({
        {name = 'id', type = 'string'},
        {name = 'dialogueId', type = 'string'},
        {name = 'fromUserId', type = 'string'},
        {name = 'toUserId', type = 'string'},
        {name = 'text', type = 'string'}
    })

    dialogue_space:create_index("pk", {parts = {'id'}, if_not_exists = true})
    dialogue_space:create_index("search", {parts = {'dialogueId'}, if_not_exists = true, unique = false})

end

function load_data(dialogueId, fromUserId, toUserId, text)
    local dialogue_space = box.space.dialogue
    dialogue_space:insert{uuid.str(), dialogueId, fromUserId, toUserId, text}
end

function get_data(dialogueId)
    return box.space.dialogue.index.search:select(dialogueId)
end

box.cfg
{
    pid_file = nil,
    background = false,
    log_level = 5
}

box.once('init', init)