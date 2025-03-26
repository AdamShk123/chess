#ifndef CHESS_EVENT_DISPATCHER_HPP
#define CHESS_EVENT_DISPATCHER_HPP

#include "event.hpp"

#include <unordered_map>
#include <vector>
#include <functional>
#include <memory>
#include <typeindex>
#include <cstdint>
#include <any>

namespace Game
{

    class EventDispatcher
    {
    public:
        using Callback = std::function<void(const void*)>;
        using Handle = std::uint64_t;

        EventDispatcher() : m_callbacks(), m_currentHandle(0) {}

        ~EventDispatcher() = default;

        template<typename T>
        auto dispatch(const T *event) -> void
        {
            for(const auto& callback : m_callbacks[std::type_index(typeid(T))])
            {
                callback.second(event);
            }
        }

        template<typename T>
        auto subscribe(const std::function<void(const T&)>& callback) -> Handle
        {
            m_callbacks[std::type_index(typeid(T))][m_currentHandle] = [callback](const auto& msg){
                const T *concrete = static_cast<const T*>(msg);
                callback(*concrete);
            };
            return m_currentHandle++;
        }

        template<typename T>
        auto unsubscribe(EventDispatcher::Handle handle) -> void
        {
            m_callbacks[std::type_index(typeid(T))].erase(handle);
        }

    private:
        std::unordered_map<std::type_index,std::unordered_map<Handle,Callback>> m_callbacks;
        Handle m_currentHandle;
    };

} // Game

#endif // CHESS_EVENT_DISPATCHER_HPP
