#ifndef CHESS_START_SCENE_HPP
#define CHESS_START_SCENE_HPP

#include "scene.hpp"
#include "event.hpp"
#include "game.hpp"
#include "interface_drawable.hpp"
#include "interface_clickable.hpp"
#include "play_button.hpp"

#include <exception>

namespace Game
{
    class Game;

    class StartScene : public Scene
    {
    public:
        explicit StartScene(Game& game);
        ~StartScene() override;

        auto enter() -> void override;
        auto update() -> std::optional<std::unique_ptr<Scene>> override;
        auto render() -> void override;
    private:
        Game& m_game;

        EventDispatcher::Handle m_escapeHandle;
        EventDispatcher::Handle m_pressHandle;

        std::optional<MousePressedEvent> m_mousePress;

        std::optional<std::unique_ptr<Scene>> m_next;

        std::vector<std::shared_ptr<Clickable>> m_clickables;
        std::vector<std::shared_ptr<Drawable>> m_drawables;

        std::unordered_map<std::string,Texture> m_textures;
    };

} // Game

#endif //CHESS_START_SCENE_HPP
