#ifndef CHESS_SCENE_HPP
#define CHESS_SCENE_HPP

#include <memory>
#include <optional>

namespace Game
{
    class Scene
    {
    public:
        virtual ~Scene() = default;

        virtual auto enter() -> void = 0;
        virtual auto update() -> std::optional<std::unique_ptr<Scene>> = 0;
        virtual auto render() -> void = 0;
    private:
    };
}

#endif //CHESS_SCENE_HPP
