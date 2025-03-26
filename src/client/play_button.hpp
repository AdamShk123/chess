#ifndef CHESS_PLAY_BUTTON_HPP
#define CHESS_PLAY_BUTTON_HPP

#include <memory>
#include "interface_drawable.hpp"
#include "interface_clickable.hpp"

#include <iostream>

namespace Game
{
    class PlayButton : public Drawable, public Clickable
    {
    public:
        PlayButton(int x, int y, const std::unique_ptr<SDL_Texture,TextureDestroyer>& texture);

        void draw() override;
        bool isWithinBounds(int x, int y) override;
        void press(const MousePressedEvent &event) override;
    private:
    };

} // Game

#endif //CHESS_PLAY_BUTTON_HPP
