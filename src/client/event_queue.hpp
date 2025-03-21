#ifndef CHESS_EVENT_QUEUE_HPP
#define CHESS_EVENT_QUEUE_HPP

#include <queue>
#include <memory>

#include "event.hpp"

namespace Game
{

    class EventQueue
    {
    public:
        EventQueue();
        ~EventQueue();

        void push(std::unique_ptr<Event> event);
        std::unique_ptr<Event> pop();

        bool empty() const;

    private:
        std::queue<std::unique_ptr<Event>> m_queue;
    };

} // Game

#endif //CHESS_EVENT_QUEUE_HPP
