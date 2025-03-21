#include "event_dispatcher.hpp"

namespace Game
{
    EventDispatcher::EventDispatcher() : m_handlers()
    {

    }

    EventDispatcher::~EventDispatcher()
    {

    }

    void EventDispatcher::subscribe(Game::EventType type, Game::EventDispatcher::EventHandler handler)
    {
        m_handlers[type].push_back(handler);
    }

    void EventDispatcher::dispatch(const std::unique_ptr<Event>& event)
    {
        EventType type = event->type;
        for (const auto& handler : m_handlers[type])
        {
            handler(event);
        }
    }
} // Game