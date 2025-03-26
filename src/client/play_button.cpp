#include "play_button.hpp"

namespace Game
{
    PlayButton::PlayButton(int x, int y, const std::unique_ptr<SDL_Texture,TextureDestroyer>& texture) : Drawable(x, y, texture) {}

    void PlayButton::draw()
    {

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