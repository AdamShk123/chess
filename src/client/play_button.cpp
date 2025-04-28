#include "play_button.hpp"

namespace Game
{
    PlayButton::PlayButton(int x, int y, const std::unique_ptr<SDL_Texture,TextureDestroyer>& texture, SDL_Renderer* renderer) : Drawable(x, y, texture, renderer) {}

    void PlayButton::draw()
    {
        const SDL_FRect dst{0, 0, 100, 100};
        SDL_RenderTexture(m_renderer, m_texture.get(), nullptr, &dst);
    }

    bool PlayButton::isWithinBounds(int x, int y)
    {
        if(x && y) return false;
        return false;
    }

    void PlayButton::press(const MousePressedEvent &event)
    {
        std::cout << typeid(event).name() << std::endl;
    }
} // Game