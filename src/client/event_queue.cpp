#include "event_queue.hpp"

namespace Game
{
    EventQueue::EventQueue() : m_queue()
    {

    }

    EventQueue::~EventQueue() = default;

    void EventQueue::push(std::unique_ptr<Event> event)
    {
        m_queue.push(std::move(event));
    }

    std::unique_ptr<Event> EventQueue::pop()
    {
        auto event = std::move(m_queue.front());
        m_queue.pop();
        return event;
    }

    bool EventQueue::empty() const
    {
        return m_queue.empty();
    }
} // Game