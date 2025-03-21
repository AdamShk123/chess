#ifndef CHESS_SCENE_HPP
#define CHESS_SCENE_HPP

namespace Game
{
    class Scene
    {
    public:
        virtual ~Scene() = default;

        virtual void enter();
    private:
    };
}

#endif //CHESS_SCENE_HPP
