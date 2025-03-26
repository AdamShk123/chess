#ifndef CHESS_INTERFACE_DRAWABLE_HPP
#define CHESS_INTERFACE_DRAWABLE_HPP

#include <SDL3/SDL_render.h>

namespace Game
{
    struct TextureDestroyer
    {
        void operator()(SDL_Texture* texture)
        {
            SDL_DestroyTexture(texture);
        }
    };

    class Drawable
    {
    public:
        Drawable(int x, int y, const std::unique_ptr<SDL_Texture,TextureDestroyer>& texture) : m_x(x), m_y(y), m_texture(texture) {}
        virtual ~Drawable() = default;
        virtual auto draw() -> void = 0;
    protected:
        int m_x;
        int m_y;

        const std::unique_ptr<SDL_Texture,TextureDestroyer>& m_texture;
    };
}

#endif //CHESS_INTERFACE_DRAWABLE_HPP
