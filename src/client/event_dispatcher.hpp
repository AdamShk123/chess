#ifndef CHESS_EVENT_DISPATCHER_HPP
#define CHESS_EVENT_DISPATCHER_HPP

#include "event.hpp"

#include <unordered_map>
#include <vector>
#include <functional>
#include <memory>

namespace Game
{

    class EventDispatcher
    {
    public:
        using EventHandler = std::function<void(const std::unique_ptr<Event>&)>;

        EventDispatcher();
        ~EventDispatcher();

        void subscribe(EventType type, EventHandler handler);
        void dispatch(const std::unique_ptr<Event>& event);

    private:
        std::unordered_map<EventType,std::vector<EventHandler>> m_handlers;
    };

} // Game

#endif //CHESS_EVENT_DISPATCHER_HPP
